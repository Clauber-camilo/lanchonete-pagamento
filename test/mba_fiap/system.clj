(ns mba-fiap.system
  (:require
    [clj-test-containers.core :as tc]
    [integrant.core :as ig]
    [mba-fiap.pagamento :as pagamento]))


(defonce db-state (atom ::not-initialized))
(defonce nats-state (atom ::not-initialized))


(defn start-mongo-container
  []
  (let [mongo-container
        (-> (tc/create {:image-name    "mongo:8.0.0-rc4"
                        :exposed-ports [27017]
                        :env-vars      {"MONGO_INITDB_ROOT_USERNAME" "root"
                                        "MONGO_INITDB_ROOT_PASSWORD" "example"}})
            (tc/start!))]
    (reset! db-state mongo-container)

    mongo-container))


(defn stop-mongo-container
  []
  (tc/stop! @db-state)
  (reset! db-state ::not-initialized))


(defn start-nats-container
  []
  (let [nats-container
        (-> (tc/create {:image-name "nats:2.10.14-alpine"
                        :exposed-ports [4222]})
            (tc/start!))]
    (reset! nats-state nats-container)

    nats-container))


(defn stop-nats-container
  []
  (tc/stop! @nats-state)
  (reset! nats-state ::not-initialized))


(defonce system-state (atom ::not-initialized))


(defn system-start
  []
  (let [conf (pagamento/prep-config :test)
        conf (-> conf
                 (assoc-in [:mba-fiap.datasource.mongo/db :spec :uri]
                           (format "mongodb://root:example@%s:%s/admin"
                                   (:host @db-state)
                                   (get (:mapped-ports @db-state) 27017)))

                 (assoc-in [[:mba-fiap.adapter.nats/nats :nats/nats] :url]
                           (format "nats://%s:%s" (:host @nats-state)
                                   (get (:mapped-ports @nats-state) 4222))))

        system (ig/init conf)]
    (tap> system)
    (reset! system-state system)
    system))


(defn system-stop
  []
  (ig/halt! @system-state)
  (reset! system-state ::not-initialized))


(comment 
  (start-nats-container)
  (start-mongo-container)
  (tap> (system-start)))
