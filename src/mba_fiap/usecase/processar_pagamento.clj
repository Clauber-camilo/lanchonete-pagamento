(ns mba-fiap.usecase.processar-pagamento
  (:require
    [clojure.edn :as edn]
    [integrant.core :as ig]
    [mba-fiap.base.validation :as validation]
    [mba-fiap.events.publisher :as publisher]
    [mba-fiap.model.pedido :refer [Pedido]]
    [mba-fiap.service.pagamento :refer [criar-pagamento]]
    [mba-fiap.usecase.processar-atualizar-status :refer [processar-atualizar-status-pagamento]]))


(defn pedido->pagamento
  [pedido]
  {:id-pedido (:id pedido)
   :total (:total pedido)
   :status "em processamento"})


(defn sleep
  [ms]
  (Thread/sleep ms))


(defn processar-novos-pedidos
  [ctx nats event]

  {:pre [(validation/schema-check Pedido (edn/read-string event))]}
  (try
    (let [repository (get-in ctx [:repository/pagamento])
          pedido (edn/read-string event)
          create-payment (criar-pagamento repository (pedido->pagamento pedido))]

      (publisher/publish-message nats {:topic (get-in ctx [:topic-new-payment])
                                       :msg create-payment})
      (sleep 500)
      (processar-atualizar-status-pagamento ctx nats (str {:_id (:_id create-payment)
                                                           :status "pago"})))
    (catch Exception e
      {:error (str "Erro ao processar pedido: " e)})))


(defmethod ig/init-key ::novos-pedidos [_ spec]
  (partial processar-novos-pedidos spec))
