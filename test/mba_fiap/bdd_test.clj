(ns mba-fiap.bdd-test
  (:require
    [clojure.edn :as edn]
    [clojure.test :refer :all]
    [fundingcircle.jukebox.alias.cucumber :as cucumber]
    [integrant.core :as ig]
    [integrant.repl.state :as state]
    [malli.generator :as mg]
    [mba-fiap.model.pagamento :as pagamento]
    [mba-fiap.service.pagamento :refer [criar-pagamento]]
    [mba-fiap.system :as system]
    [mba-fiap.usecase.processar-atualizar-status :refer [processar-atualizar-status-pagamento]])
  (:import
    (mba_fiap.adapter.nats
      INATSClient)))


(defn repository
  [repository-key]
  (->> (ig/find-derived state/system :mba-fiap.repository.repository/repository)
       (filter (fn [[[_ rk]]] (= rk repository-key)))
       first
       second))


(comment (tap> (repository :repository/pagamento)))


(defn mock-nats
  []
  (proxy [INATSClient] []
    (publish
      [subject msg]
      {:subject subject :msg msg})))


(defn extract
  []
  (let [data {[:mba-fiap.repository.repository/repository :repository/pagamento]
              {:test "true "}}
        {e [:mba-fiap.repository.repository/repository :repository/pagamento]} data]

    (tap> {:e  e})))


(comment (extract))


;; BDDs

(defn i-have-a-payment
  "Sets up a new payment "
  {:scene/step "I have a payment"}
  [board]
  (system/start-nats-container)
  (system/start-mongo-container)
  (let [start (system/system-start)
        {repository [:mba-fiap.repository.repository/repository :repository/pagamento]} start]

    (Thread/sleep 4000)
    {:ctx {:repository/pagamento repository}
     :nats (mock-nats)
     :event (str (criar-pagamento
                   repository
                   (assoc (mg/generate pagamento/Pagamento) :status "pago")))}))


(defn i-receive-a-new-event
  "Returns the update context ('board') "
  {:scene/step "I receive a new event"}
  [board]
  (let [{:keys [ctx nats event]} board
        processar (processar-atualizar-status-pagamento ctx nats event)]

    (if (empty? (:msg processar))
      (throw (Exception. "Failed to process the event"))
      (assoc board :msg (:msg processar)))))


(defn i-should-change-the-status-to
  "Validate the status change"
  {:scene/step "I should change the status to {string}"}
  [board x]
  (let [status (:status (edn/read-string (:msg board)))]
    (if (not= status x)
      (throw (Exception. (str "Status should be " x " but was " status)))
      board)
    (system/system-stop)
    (system/stop-mongo-container)
    (system/stop-nats-container)))


(defn run-cucumber
  []
  (cucumber/-main "-g" "./test/mba_fiap/" "./test/resources/"))


(comment (run-cucumber))
