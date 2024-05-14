(ns mba-fiap.usecase.processar-pagamento-test
  (:require
    [clojure.test.check.clojure-test :refer [defspec]]
    [clojure.test.check.properties :as prop]
    [malli.generator :as mg]
    [mba-fiap.events.publisher :as publisher]
    [mba-fiap.model.pedido :as pedido]
    [mba-fiap.usecase.processar-pagamento :refer [pedido->pagamento
                                                  processar-novos-pedidos]])
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


(defspec test-processar-novos-pedidos 100
  (prop/for-all [pedido (mg/generator pedido/Pedido)]
                (with-redefs [publisher/publish-message (fn [_ data] data)]
                  (let [id (java.util.UUID/randomUUID)
                        result (processar-novos-pedidos
                                 {:repository/pagamento (mock-repository)
                                  :topic "test-topic"}
                                 (mock-nats)
                                 (str (assoc pedido :id id)))]
                    (and (nil? (:error result))
                         (= (:id-pedido result) (:id id)))))))


(comment (test-pedido->pagamento)
        (test-processar-novos-pedidos))
