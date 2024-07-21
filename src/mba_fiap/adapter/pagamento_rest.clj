(ns mba-fiap.adapter.pagamento-rest
  (:require
    [io.pedestal.http.body-params :as body-params]
    [io.pedestal.http.ring-middlewares :as middlewares]
    [mba-fiap.datasource.mongo :as mongo]))


(def default-interceptors
  [(body-params/body-params)
   middlewares/params
   middlewares/keyword-params])


(defn pagamento-routes
  "Routes definition for the api"
  []
  [["/healthcheck"
    ^:interceptors `default-interceptors {:get `healthcheck}]])


(defn healthcheck
  [request]
  (let [ctx (get-in request [:app-context])
        db (get-in request [:app-context :repository/pagamento :db])
        res (mongo/check-health db)]
    (if (= (:status res) "ok")
      {:status 200
       :body (str ctx)}
      {:status 500
       :body "error"})))
