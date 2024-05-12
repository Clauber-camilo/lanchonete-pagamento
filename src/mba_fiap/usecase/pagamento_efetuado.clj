(ns mba-fiap.usecase.pagamento-efetuado
  (:require
    [mba-fiap.events.publisher :as publisher])
  (:import
    (mba_fiap.adapter.nats
      INATSClient)))


(defn publicar-mensagem
  [^INATSClient nats]
  (tap> nats)
  (doseq [r (range 10)]
    (Thread/sleep 500)
    (.publish nats r "YAMETE KUDASAI")))


(defmethod publisher/make-publisher :pagamento-efetuado
  [{:keys [nats]}]
  (publicar-mensagem nats))
