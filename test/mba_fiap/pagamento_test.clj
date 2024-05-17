(ns mba-fiap.pagamento-test
  (:require
    [aero.core :as aero]
    [clj-test-containers.core :as tc]
    [clojure.test :refer [deftest is testing use-fixtures]]
    [hato.client :as hc]
    [integrant.core :as ig]
    [mba-fiap.pagamento :as pagamento]))


(defn mock-read-config
  [profile]
  {:mock-config true})


(defn mock-prep-config
  [profile]
  {:mock-prepared-config true})


(defn mock-start-app
  [profile]
  {:mock-app true})


(deftest test-read-config
  (with-redefs [aero/read-config (fn [_ _] {:mock-config true})]
    (is (= {:mock-config true} (pagamento/read-config :test)))))


(deftest test-prep-config
  (with-redefs [pagamento/read-config mock-read-config
                ig/load-namespaces (fn [_] nil)
                ig/prep (fn [_] {:mock-prepared-config true})]
    (is (= {:mock-prepared-config true} (pagamento/prep-config :test)))))


(deftest test-start-app
  (with-redefs [pagamento/prep-config mock-prep-config
                ig/init (fn [_] {:mock-app true})]
    (is (= {:mock-app true} (pagamento/start-app :test)))))


(deftest test-main
  (with-redefs [pagamento/start-app mock-start-app]
    (is (= {:mock-app true} (pagamento/-main "test")))))


(defonce db-state (atom ::not-initialized))


(defn mongo-fixture
  [f]
  (let [mongo-container
        (-> (tc/create {:image-name    "mongo:8.0.0-rc4"
                        :exposed-ports [27017]
                        :env-vars      {"MONGO_INITDB_ROOT_USERNAME" "root"
                                        "MONGO_INITDB_ROOT_PASSWORD" "example"}})
            (tc/start!))]
    (reset! db-state mongo-container)
    (try
      (f)
      (catch Exception e
        (prn e)))
    (tc/stop! mongo-container)
    (reset! db-state ::not-initialized)))


(defonce system-state (atom ::not-initialized))


(defn system-fixture
  [f]
  (let [conf (pagamento/prep-config :test)
        conf (-> conf
                 (assoc-in [:mba-fiap.datasource.mongo/db :spec :uri]
                           (format "mongodb://root:example@%s:%s/admin"
                                   (:host @db-state)
                                   (get (:mapped-ports @db-state) 5432))))
        _ (tap> conf)
        system (ig/init conf)]
    (reset! system-state system)
    (try
      (f)
      (catch Exception e
        (prn e)))
    (ig/halt! system)
    (reset! system-state ::not-initialized)))


(use-fixtures :once mongo-fixture)
(use-fixtures :once system-fixture)


(deftest test-main-startup
  (testing "main startup ok"
    (let [{:keys [body status]} (hc/get "http://localhost:8080/healthcheck")]
      (is (= 200 status))
      (is (= "[]" body)))))


(comment 
  (def mongo (-> (tc/create {:image-name    "mongo:8.0.0-rc4"
                        :exposed-ports [27017]
                        :env-vars      {"MONGO_INITDB_ROOT_USERNAME" "root"
                                        "MONGO_INITDB_ROOT_PASSWORD" "example"}})
            (tc/start!)))
  (test-read-config)
         (mongo-fixture (fn [] nil))
         (test-prep-config)
         (test-start-app)
         (test-main))
