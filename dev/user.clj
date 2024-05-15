(ns user
  (:require
    [hato.client :as hc]
    [integrant.core :as ig]
    [integrant.repl :as r]
    [integrant.repl.state]
    [mba-fiap.adapter.nats :refer [nats-client]]
    [mba-fiap.pagamento :as pagamento])
  (:import
    (org.bson.types
      ObjectId)))


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
                   {:_id (ObjectId. "6644048c7405ae6c8eb88a4a")
                    :total 234
                    :id-pedido #uuid"fbb98663-77ab-4560-a065-6b9b833c190f"
                    :status "pago"})
   (.buscar (repository :repository/pagamento)
                   (ObjectId. "6643f32a7405aed48ae9cbca"))
   (.remover (repository :repository/pagamento)
                  "1234"))


(comment (hc/get "http://localhost:8000/healthcheck"))


(comment

  (with-open [c (nats-client {:url               "nats://66.51.121.86:4222"
                              :app-name          "lanchonete"
                              ; :subjects-handlers {"lanchonete.*" #(prn (.getSubject %) " " (String. (.getData %)) "----" (bean %) "\n" (str %))}})]
                              :subjects-handlers {
                                                  ; "lanchonete.novo-pedido" 
                                                  ; #(prn (String. (.getData %)))
                                                  }})]

    (doseq [_ (range 1)]
      (Thread/sleep 500)
      (.publish c "novo-pedido" 
               (str {
                    :id #uuid "2985094e-43ea-4105-8e4e-239913f72d33" 
                    :id-cliente #uuid "01c1e2be-3ce6-4ff6-9a88-6c75124840b0"
                    :numero-do-pedido "fbb98663-77ab-4560-a065-6b9b833c190f"
                    :produtos [#uuid "f9cb3907-267d-4a5c-9a18-bbd0381b2720"]
                    :status "aguardando pagamento"
                    :total 1238
                    }))))
  )
