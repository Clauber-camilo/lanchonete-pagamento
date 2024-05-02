(ns mba-fiap.adapter.pagamento-rest
  (:require
    [clojure.data.json :as json]
    [io.pedestal.http.body-params :as body-params]
    [io.pedestal.http.ring-middlewares :as middlewares]))


(defn buscar-por-id-pedido
  [request]
  (tap> {:title "request" :data request})
  {:status 404
   :headers {"Content-Type" "application/json"}
   :body (json/write-str {:sucess false})})


(defn pagamento-routes
  []
  [["/pagamento" ^:interceptors [(body-params/body-params)
                                 middlewares/keyword-params]
    {:get `buscar-por-id-pedido}]])
