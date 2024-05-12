(ns mba-fiap.usecase.processar-pagamento
  (:require
    [clojure.edn :as edn]
    [integrant.core :as ig]))


(defn processar-novos-pedidos
  [ctx event]
  (tap> {:from "processar-novos-pedidos"
         :event event
         :ctx ctx})
  (tap>  (edn/read-string event)))


(comment 
   (processar-novos-pedidos 
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
