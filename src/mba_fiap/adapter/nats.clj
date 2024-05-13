(ns mba-fiap.adapter.nats
  (:require
    [integrant.core :as ig])
  (:import
    (io.nats.client
      Connection$Status
      Message
      MessageHandler
      Nats
      Options)
    (java.io
      Closeable)
    (java.nio.charset
      StandardCharsets)))


(defprotocol INATSClient

  (publish [_ subject msg]))


(defrecord NATSClient
  [app-name connection dispatchers]

  Closeable

  (close
    [_]
    (run! #(.closeDispatcher connection %) dispatchers)
    (.close connection))


  INATSClient

  (publish
    [_ subject msg]
    (tap> "FOOOOOOI")
    (let [subject (str app-name "." subject)
          reply-to (str subject ".reply")]

      (prn reply-to)
      (.publish connection
                subject
                reply-to
                (.getBytes msg StandardCharsets/UTF_8)))))


(defn nats-client
  [{:keys [app-name url subjects-handlers]}]
  (let [connection (Nats/connect (-> (Options/builder)
                                     (.server url)
                                     (.build)))
        ->dispatcher (fn [f]
                       (reify MessageHandler
                         (^void onMessage [_ ^Message msg]
                           (f (->NATSClient app-name connection nil) (String. (.getData msg))))))
        dispatchers (->> subjects-handlers
                         (mapv (fn [[subject handler]]
                                 (doto (.createDispatcher connection (->dispatcher handler))
                                   (.subscribe subject)))))]
    (loop [status (.getStatus connection)]
      (if (= status Connection$Status/CONNECTED)
        connection
        (recur (.getStatus connection))))

    (->NATSClient app-name connection dispatchers)))


(defmethod ig/init-key ::nats
  [_ cfg]
  (nats-client cfg))


(defmethod ig/halt-key! ::nats [_ nats]
  (.close nats))


(defmethod ig/resolve-key ::nats
  [_ nats]
  nats)
