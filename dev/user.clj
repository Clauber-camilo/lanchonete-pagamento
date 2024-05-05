(ns user
  (:require
    [hato.client :as hc]
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


(defn listar-pagamento
  [id]
  (hc/get (str "http://localhost:8000/pagamento/" id)))


(comment (listar-pagamento 1234))
