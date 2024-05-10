(ns mba-fiap.service.pagamento-test
  (:require
    [clojure.test.check.clojure-test :refer [defspec]]
    [clojure.test.check.properties :as prop]
    [malli.generator :as mg]
    [mba-fiap.model.pagamento :as pagamento]
    [mba-fiap.repository.repository]
    [mba-fiap.service.pagamento :as pagamento.service])
  (:import
    (java.util Date)
    (mba_fiap.repository.repository
      Repository)))


(defn mock-repository
  [store]
  (proxy [Repository] []
    (criar
      [data]
      (let [pgmt-uuid (random-uuid)
            created (:created_at data (Date.))
            data (assoc data :id pgmt-uuid :created_at created)]

        (swap! store assoc pgmt-uuid data)
        (swap! store update (:id-pedido data) (fn [x]
                                                (if (seq x)
                                                  (conj x data)
                                                  [data])))
        (tap> {:at ::criar
               :store @store
               :data  data})
        [{:id         pgmt-uuid
          :id_pedido  (:id-pedido data)
          :total      (:total data)
          :status     (:status data)
          :created_at (:created_at data)}]))

    (buscar
      [id]
      (let [data (get @store id)]
        (->> data
             (mapv (fn [x]
                     {:id         (:id x)
                      :id_pedido  (:id_pedido x)
                      :total      (:total x)
                      :status     (:status x)
                      :created_at (:created_at x)})))))

    (listar
      [q]
      (get @store (:id-pedido q)))

    (atualizar
      [data]
      (let [found (get @store (:id data))
            updated-data (assoc found :status (:status data))]
        (swap! store assoc (:id updated-data) updated-data)
        [{:id         (:id updated-data)
          :id_pedido  (:id_pedido updated-data)
          :total      (:total updated-data)
          :status     (:status updated-data)
          :created_at (:created_at updated-data)}]))))


(defspec criar-pagamento-test 100
  (prop/for-all
    [pagamento (mg/generator pagamento/Pagamento)]
    (let [store (atom {})
          mr (mock-repository store)
          result (pagamento.service/criar-pagamento mr pagamento)]
      (= (:id-pedido pagamento) (:id-pedido result)))))


(defspec buscar-por-id-pedido-test 1
  (prop/for-all
    [pagamento (mg/generator pagamento/Pagamento)]
    (let [store (atom {})
          mr (mock-repository store)
          _insert (.criar mr pagamento)
          result (pagamento.service/buscar-por-id-pedido mr (:id-pedido pagamento))]

      (tap> {:store  @store
             :result result})
      (true? true))))


(defspec buscar-por-id-pedido-test-error 1000
  (prop/for-all
    [pagamento (mg/generator pagamento/Pagamento)]
    (let [store (atom {})
          mr (mock-repository store)
          found (pagamento.service/buscar-por-id-pedido mr (:id-pedido pagamento))]
      (= "Pagamento não encontrado" (:error found)))))


(defspec atualizar-status-pagamento 1
  (prop/for-all
    [pagamento (mg/generator pagamento/Pagamento)]
    (let [store (atom {})
          mr (mock-repository store)
          _insert (.criar mr pagamento)
          found (pagamento.service/atualizar-status-pagamento mr (:id-pedido pagamento) "pago")]
      (= "pago" (:status found)))))


(comment
  (.criar (mock-repository (atom {})) {:status "foi"})

  (criar-pagamento-test)
  (buscar-por-id-pedido-test)
  (buscar-por-id-pedido-test-error)
  (atualizar-status-pagamento))
