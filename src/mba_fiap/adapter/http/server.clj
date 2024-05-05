(ns mba-fiap.adapter.http.server
  (:require
    [integrant.core :as ig]
    [io.pedestal.http :as http]
    [io.pedestal.http.route :as route]
    [io.pedestal.interceptor :as interceptor]
    [mba-fiap.adapter.pagamento-rest :as pagamento-rest]))


;; (defn context-interceptor
;;   [context]
;;   (interceptor/interceptor
;;     {:name ::include-context
;;      :enter #(assoc % :app-context context)}))

(defn context-interceptor
  [context]
  (interceptor/interceptor
    {:name ::include-context
     :enter (fn [x]
              (let [request (get-in x [:request])
                    update-request (assoc request :app-context context)]
                (assoc x :request update-request)))}))


(defn tap-interceptor
  []
  (interceptor/interceptor {:enter #(doto % tap>)}))


(def tap-error-interceptor
  (interceptor/interceptor
    {:leave (fn [x]
              (tap> [::dev-logging x])
              x)}))


(defn routes
  []
  (route/expand-routes
    (into []
          [(pagamento-rest/pagamento-routes)])))


(defn add-interceptors
  [service-map & interceptors]
  (update service-map
          :io.pedestal.http/interceptors
          #(vec (concat % interceptors))))


(defn server
  [{:keys [env port join? app-context]}]
  (println "Starting server")
  (let [ctx-interceptor (context-interceptor app-context)]
    (cond-> {:env env
             ::http/routes (routes)
             ::http/resource-path "/public"
             ::http/type :jetty
             ::http/join? join?
             ::http/port port
             ::http/host "0.0.0.0"}
      :always http/default-interceptors
      :always (add-interceptors ctx-interceptor http/json-body (tap-interceptor))
      (or (= :dev env)
          (= :test env)) (-> http/dev-interceptors
                             (add-interceptors tap-error-interceptor))
      :then http/create-server)))


(defmethod ig/init-key ::server [_ cfg]
  (http/start (server cfg)))


(defmethod ig/halt-key! ::server [_ server]
  (http/stop server))
