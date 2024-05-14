(ns mba-fiap.usecase.processar-pagamento
  (:require
    [clojure.edn :as edn]
    [integrant.core :as ig]
    [mba-fiap.base.validation :as validation]
    [mba-fiap.events.publisher :as publisher]
    [mba-fiap.model.pedido :refer [Pedido]]
    [mba-fiap.service.pagamento :refer [criar-pagamento]]))


(defn pedido->pagamento
  [pedido]
  {:id-pedido (:id pedido)
   :total (:total pedido)
   :status "em processamento"})


(defn processar-novos-pedidos
  [ctx nats event]

  {:pre [(validation/schema-check Pedido (edn/read-string event))]}

  (let [repository (get-in ctx [:repository/pagamento])
        pedido (edn/read-string event)
        create-payment (criar-pagamento repository (pedido->pagamento pedido))]

    (if (empty? create-payment)
      {:error "Erro ao criar pagamento"}

      (publisher/publish-message nats {:topic (get-in ctx [:topic])
                                       :msg create-payment}))))


(defn processar-atualizar-pedido
  [nats event]
  {:sucess true})


(comment 
   (processar-novos-pedidos 
     nil
     nil
     (str { :id #uuid "2985094e-43ea-4105-8e4e-239913f72d33" 
            :id-cliente #uuid "01c1e2be-3ce6-4ff6-9a88-6c75124840b0"
            :numero-do-pedido "fbb98663-77ab-4560-a065-6b9b833c190f"
            :produtos nil
            :status "recebido"
            :total 1238
            :created-at nil}) ))


(defmethod ig/init-key ::novos-pedidos [_ spec]
  (partial processar-novos-pedidos spec))
