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
                           (f (String. (.getData msg))))))
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
  (prn (str "Config :: " cfg))
  (nats-client cfg))


(defmethod ig/halt-key! ::nats [_ nats]
  (.close nats))


(defmethod ig/resolve-key ::nats
  [_ nats]
  nats)


(comment

  (with-open [c (nats-client {:url               "nats://66.51.121.86:4222"
                              :app-name          "lanchonete"
                              ; :subjects-handlers {"lanchonete.*" #(prn (.getSubject %) " " (String. (.getData %)) "----" (bean %) "\n" (str %))}})]
                              :subjects-handlers {
                                                  ; "lanchonete.novo-pedido" 
                                                  ; #(prn (String. (.getData %)))
                                                  }})]

    (doseq [_ (range 1)]
      (Thread/sleep 500)
      (publish c "novo-pedido" 
               (str {
                    :id #uuid "2985094e-43ea-4105-8e4e-239913f72d33" 
                    :id-cliente #uuid "01c1e2be-3ce6-4ff6-9a88-6c75124840b0"
                    :numero-do-pedido "fbb98663-77ab-4560-a065-6b9b833c190f"
                    :produtos nil
                    :status "recebido"
                    :total 1238
                    :created-at nil}))))
  )
