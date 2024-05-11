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
                           (f msg))))
        dispatchers (->> subjects-handlers
                         (mapv (fn [[subject handler]]
                                 (doto (.createDispatcher connection (->dispatcher handler))
                                   (.subscribe subject)))))]
    (loop [status (.getStatus connection)]
      (prn "NATS connection status: " status)
      (if (= status Connection$Status/CONNECTED)
        connection
        (recur (.getStatus connection))))

    (->NATSClient app-name connection dispatchers)))


(defmethod ig/init-key ::nats
  [_ cfg]
  (prn (str "Config ::" cfg))
  (nats-client cfg #_(assoc cfg :subjects-handlers {"lanchonete.novos-pedidos" #(prn (.getSubject %) " " (String. (.getData %)) "----" (bean %))})))


(defmethod ig/halt-key! ::nats [_ nats]
  (.close nats))


(comment
  (bean (.getStatistics (:connection c)))

  (def c (nats-client {:url               "nats://66.51.121.86:4222"
                              :app-name          "fodase"
                              :subjects-handlers {"lanchonete.novos-pedidos" #(prn (.getSubject %) " " (String. (.getData %)) "----" (bean %))}}))

  (with-open [c (nats-client {:url               "nats://66.51.121.86:4222"
                              :app-name          "fodase"
                              :subjects-handlers {"lanchonete.*" #(prn (.getSubject %) " " (String. (.getData %)) "----" (bean %))}})]

    (doseq [r (range 0)]
      (Thread/sleep 500)
      #_(publish c r "YAMETE KUDASAI")))
  )
