(ns migration.mongo
  (:require
    [aero.core :as aero]
    [clojure.java.io :as io]
    [integrant.core :as ig]))


(def config (slurp "config.edn"))


;; (defmethod aero/reader 'ig/ref
;;   [{:keys [profile] :as opts} _tag value]
;;   (ig/ref value))
;;
;;
;; (defn read-config
;;   [profile]
;;   (aero/read-config (io/resource system-filename) {:profile profile}))
;;
;;
;; (defn prep-config
;;   [profile]
;;   (let [config-map (read-config profile)]
;;     (ig/load-namespaces config-map)
;;     (ig/prep config-map)))
;;
;;
;; (defn exec
;;   [& args]
;;   (tap> args)
;;   (println "YAHOOOO")
;;   (let [profile (or (some-> args first keyword) :prod)]
;;     (println "Running, profile: " profile)
;;     (-> (prep-config profile)
;;         (ig/init))))
;;

(defn exec
  [args]
  (println "exec migrations")
  (println args)
  (ig/init config))


;; (defn -main
;;   [& args]
;;   (let [profile (or (some-> args first keyword) :prod)]
;;     (println "Running, profile: " profile)
;;     (start-app profile)))
