(ns user
  (:require
    [clojure.data.json :as json]
    [hato.client :as hc]
    [integrant.core :as ig]
    [integrant.repl :as r]
    [integrant.repl.state]
    [mba-fiap.pagamento :as pagamento]))


(integrant.repl/set-prep! #(pagamento/prep-config :dev))

(def clear r/clear)
(def go r/go)
(def halt r/halt)
(def prep r/prep)
(def init r/init)
(def reset r/reset)
(def reset-all r/reset-all)


(defn portal
  []
  (eval '(do
           (require '[portal.api :as api])
           (add-tap api/submit)
           (api/open))))


;; integrant.repl.state/system

(defn repository
  [repository-key]
  (->> (ig/find-derived integrant.repl.state/system :mba-fiap.repository.repository/repository)
       (filter (fn [[[_ rk]]] (= rk repository-key)))
       first
       second))


(comment
   (reset-all)
   (.listar (repository :repository/pagamento) {:status "pago"})
   (.criar (repository :repository/pagamento)
                 {:id-pedido #uuid"fbb98663-77ab-4560-a065-6b9b833c190f"
                  :status "em processamento"})
   (.atualizar (repository :repository/pagamento)
                   {:id-pedido #uuid"fbb98663-77ab-4560-a065-6b9b833c190f"
                    :status "pago"})
   (.buscar (repository :repository/pagamento)
                   #uuid"fbb98663-77ab-4560-a065-6b9b833c190f")
   (.remover (repository :repository/pagamento)
                  "1234"))


(comment (hc/get "http://localhost:8000/healthcheck"))


;; (def pagamento-payload
;;   {:id-pedido "fbb98663-77ab-4560-a065-6b9b833c190f"
;;    :status "em processamento"})
;;
;;
;; (defn buscar-pagamento
;;   [id]
;;   (hc/get (str "http://localhost:8000/pagamento/" id)))
;;
;;
;; (defn criar-pagamento
;;   [pagamento]
;;   (hc/post "http://localhost:8000/pagamento" {:body (json/write-str pagamento)}))
;;
;;
;; (defn atualizar-pagamento
;;   [id status]
;;   (hc/put (str "http://localhost:8000/pagamento/" id) {:status status}))
;;
;;
;; (comment
;;          (criar-pagamento pagamento-payload)
;;          (buscar-pagamento (:id-pedido pagamento-payload))
;;          (atualizar-pagamento (:id-pedido pagamento-payload) "pago"))
