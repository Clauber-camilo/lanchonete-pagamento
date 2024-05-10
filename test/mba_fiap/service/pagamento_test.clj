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
    (mba_fiap.repository.repository Repository)))


(defn mock-repository
  [store]
  (proxy [Repository] []
    (criar
      [data]
      (let [pgmt-uuid (random-uuid)
            created (:created-at data (Date.))
            data (assoc data :id pgmt-uuid :created-at created)]

        (swap! store assoc pgmt-uuid data)
        (swap! store update (:id-pedido data) (fn [x]
                                                (if (seq x)
                                                  (conj x data)
                                                  [data])))
        {:id         pgmt-uuid
         :id-pedido  (:id-pedido data)
         :total      (:total data)
         :status     (:status data)
         :created-at (:created-at data)}))

    (buscar
      [id]
      (get @store id))

    (listar
      [q]
      (let [data (get @store q)]
        (->> data
             (mapv (fn [x]
                     {:id         (:id x)
                      :id-pedido  (:id-pedido x)
                      :total      (:total x)
                      :status     (:status x)
                      :created-at (:created-at x)})))))

    (atualizar
      [data]
      (let [found (get @store (:id data))
            updated-data (assoc found :status (:status data))]
        (swap! store assoc (:id updated-data) updated-data)
        {:id         (:id updated-data)
         :id-pedido  (:id-pedido updated-data)
         :total      (:total updated-data)
         :status     (:status updated-data)
         :created-at (:created-at updated-data)}))))


(defspec criar-pagamento-test 1
  (prop/for-all
    [pagamento (mg/generator pagamento/Pagamento)]
    (let [store (atom {})
          mr (mock-repository store)
          result (pagamento.service/criar-pagamento mr pagamento)]
      (= (:id-pedido pagamento) (:id-pedido result)))))


(defspec buscar-por-id-pedido-test 1000
  (prop/for-all
    [pagamento (mg/generator pagamento/Pagamento)]
    (let [store (atom {})
          mr (mock-repository store)
          _insert (.criar mr pagamento)
          [result] (pagamento.service/buscar-por-id-pedido mr (:id-pedido pagamento))]

      (= (:id-pedido pagamento) (:id-pedido result)))))


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
          {:keys [id]} (.criar mr pagamento)
          found (pagamento.service/atualizar-status-pagamento mr id "pago")]
      (tap> [pagamento found])
      (= "pago" (:status found)))))


(comment
  (.criar (mock-repository (atom {})) {:status "foi"})

  (criar-pagamento-test)
  (buscar-por-id-pedido-test)
  (buscar-por-id-pedido-test-error)
  (atualizar-status-pagamento))
