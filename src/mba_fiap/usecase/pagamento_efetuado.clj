(ns mba-fiap.usecase.pagamento-efetuado
  (:require
    [integrant.core :as ig]))


(defn publicar-mensagem
  [ctx nats]
  (tap> {:from "publicar-mensagem"
         :ctx ctx
         :nats nats})
  (doseq [_ (range 10)]
    (Thread/sleep 500)
    (.publish nats "testing" "YAMETE KUDASAI")))


(defmethod ig/init-key ::publicar-mensagem [_ spec]
  (partial publicar-mensagem spec))
