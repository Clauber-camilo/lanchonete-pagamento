(ns mba-fiap.adapter.pagamento-rest
  (:require
    [io.pedestal.http.body-params :as body-params]
    [io.pedestal.http.ring-middlewares :as middlewares]
    [mba-fiap.service.pagamento :as pagamento.service]))


;; (def default-interceptors
;;   [(body-params/body-params)
;;    middlewares/params
;;    middlewares/keyword-params])

(def default-interceptors
  [(body-params/body-params)
   middlewares/params
   middlewares/keyword-params])


(defn pagamento-routes
  "Routes definition for the api"
  []
  [["/pagamento/:id-pedido"
    ^:interceptors `default-interceptors {:get `buscar-por-id-pedido}]
   ["/pagamento"
    ^:interceptors [(body-params/body-params)]
    {:post `criar-pagamento}]
   ["/pagamento/:id-pedido" `default-interceptors
    {:put `atualizar-status-pagamento}]])


(defn criar-pagamento
  [request]
  (let [repository (get-in request [:app-context :repository/pagamento])
        data (:json-params request)
        parsed-data (-> data
                        (update :id-pedido parse-uuid))
        result (pagamento.service/criar-pagamento repository parsed-data)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body result}))


(defn atualizar-status-pagamento
  [request]
  (let [repository (get-in request [:app-context :repository/pagamento])
        id-pedido (get-in request [:path-params :id-pedido])
        status (get-in request [:json-params :status])
        result (pagamento.service/atualizar-status-pagamento repository (parse-uuid id-pedido) status)]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    result}))


(defn buscar-por-id-pedido
  [request]
  (let [repository (get-in request [:app-context :repository/pagamento])
        {:keys [id-pedido]} (:path-params request)
        result (pagamento.service/buscar-por-id-pedido repository id-pedido)]

    {:status 200
     :body result}))
