(ns mba-fiap.usecase.processar-pagamento
  (:require
    [integrant.core :as ig]))


(defn processar-novos-pedidos
  [event]
  (prn (.getSubject event) " " (String. (.getData event)) "----" (bean event)))


(defmethod ig/init-key ::novos-pedidos [_ _]
  processar-novos-pedidos)
