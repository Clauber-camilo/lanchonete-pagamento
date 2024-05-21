(ns mba-fiap.pagamento-test
  (:require
    [aero.core :as aero]
    [clojure.test :refer [deftest is testing use-fixtures]]
    [hato.client :as hc]
    [integrant.core :as ig]
    [mba-fiap.pagamento :as pagamento]
    [mba-fiap.system :as system]))


(defn mongo-fixture
  [f]
  (let [_ (system/start-mongo-container)]

    (try
      (f)
      (catch Exception e
        (prn e))
      (finally (system/stop-mongo-container)))))


(defn nats-fixture
  [f]
  (let [_ (system/start-nats-container)]

    (try
      (f)
      (catch Exception e
        (prn e))
      (finally (system/stop-nats-container)))))


(defn system-fixture
  [f]
  (let [_system (system/system-start)]
    (try
      (f)
      (catch Exception e
        (prn e))
      (finally (system/system-stop)))))


(use-fixtures :once nats-fixture mongo-fixture system-fixture)


(deftest test-main-startup
  (testing "main startup ok"
    (let [{:keys [body status]} (hc/get "http://localhost:8080/healthcheck")]
      (is (= 200 status)))))


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
