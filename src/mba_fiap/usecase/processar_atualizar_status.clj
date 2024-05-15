(ns mba-fiap.usecase.processar-atualizar-status
  (:require
    [clojure.edn :as edn]
    [mba-fiap.events.publisher :as publisher]
    [mba-fiap.service.pagamento :refer [atualizar-status-pagamento]]))


(defn processar-atualizar-status-pagamento
  [ctx nats event]

  (let [repository (get-in ctx [:repository/pagamento])
        {:keys [_id status]} (edn/read-string event)
        update-payment (atualizar-status-pagamento repository _id status)]

    (if (nil? update-payment)
      {:error "Erro ao atualizar pagamento"}
      (publisher/publish-message nats {:topic (get-in ctx [:topic-update-status])
                                       :msg update-payment}))))
