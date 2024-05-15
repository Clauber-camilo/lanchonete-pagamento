(ns mba-fiap.events.publisher)


(defn publish-message
  [nats {:keys [topic msg]}]
  (.publish nats topic (str msg)))
