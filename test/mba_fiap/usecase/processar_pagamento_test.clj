(ns mba-fiap.usecase.processar-pagamento-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [clojure.test.check.clojure-test :refer [defspec]]
    [clojure.test.check.properties :as prop]
    [malli.generator :as mg]
    [mba-fiap.events.publisher :as publisher]
    [mba-fiap.model.pedido :as pedido]
    [mba-fiap.usecase.processar-atualizar-status :refer [processar-atualizar-status-pagamento]]
    [mba-fiap.usecase.processar-pagamento :refer [pedido->pagamento
                                                  processar-novos-pedidos sleep]])
  (:import
    (mba_fiap.adapter.nats
      INATSClient)
    (mba_fiap.repository.repository
      Repository)))


(defn mock-nats
  []
  (proxy [INATSClient] []
    (publish
      [subject msg]
      {:subject subject :msg msg})))


(defn mock-repository
  []
  (proxy [Repository] []
    (criar
      [data]
      data)

    (buscar
      [id]
      {:id id})

    (listar
      [q]
      [{:id q}])

    (atualizar
      [data]
      data)))


(defspec test-pedido->pagamento 100
  (prop/for-all [pedido (mg/generator pedido/Pedido)]
                (let [result (pedido->pagamento pedido)]
                  (= result {:id-pedido (:id pedido)
                             :total (:total pedido)
                             :status "em processamento"}))))


(defspec test-processar-novos-pedidos 1000
  (prop/for-all [pedido (mg/generator pedido/Pedido)]
                (with-redefs [publisher/publish-message (fn [_ data] data)
                              sleep (fn [_] nil)
                              processar-atualizar-status-pagamento (fn [_ _ data] data)]
                  (let [id (java.util.UUID/randomUUID)
                        result (processar-novos-pedidos
                                 {:repository/pagamento (mock-repository)
                                  :topic "test-topic"}
                                 (mock-nats)
                                 (str (assoc pedido :id id)))]
                    (and (nil? (:error result))
                         (= (:id-pedido result) (:id id)))))))


(deftest test-sleep
  (testing "Sleeps for specified time"
    (let [start (System/currentTimeMillis)
          _ (sleep 100)
          end (System/currentTimeMillis)]
      (is (>= (- end start) 100)))))


(defspec test-processar-novos-pedidos-error 100
  (prop/for-all [pedido (mg/generator pedido/Pedido)]
                (let [pedido (assoc pedido :id (java.util.UUID/randomUUID))
                      result (processar-novos-pedidos {:repository/pagamento nil
                                                       :topic "test-topic"}
                                                      (mock-nats)
                                                      (str pedido))]
                  (some? (re-find #"Erro ao processar pedido: " (:error result))))))


(comment (test-pedido->pagamento)
         (test-processar-novos-pedidos)
         (test-sleep)
         (test-processar-novos-pedidos-error))
