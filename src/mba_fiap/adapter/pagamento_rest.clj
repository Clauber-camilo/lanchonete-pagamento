(ns mba-fiap.adapter.pagamento-rest
  (:require
    [io.pedestal.http.body-params :as body-params]
    [io.pedestal.http.ring-middlewares :as middlewares]))


(defn buscar-por-id-pedido
  [request]
  {:status 200
   :body {:sucess true :test {:value 100}}})


(defn pagamento-routes
  []
  [["/pagamento" ^:interceptors [(body-params/body-params)
                                 middlewares/params
                                 middlewares/keyword-params]
    {:get `buscar-por-id-pedido}]])
