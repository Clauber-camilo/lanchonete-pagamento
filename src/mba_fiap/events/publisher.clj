(ns mba-fiap.events.publisher)


(defn publish-message
  [nats {:keys [topic msg]}]
  (tap> {:from "publish-message"
         :msg msg
         :topic topic
         :nats nats})
  (.publish nats topic (str msg)))
