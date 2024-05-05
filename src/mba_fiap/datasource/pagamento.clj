(ns mba-fiap.datasource.pagamento
  (:require
    [mba-fiap.repository.repository :as repository]))


(defrecord PagamentoDatasource
  [connection]

  repository/Repository

  (criar
    [_ data]
    (println "Criando pagamento"))


  (buscar
    [_ id]
    (println "Buscando pagamento")
    {:sucess true
     :id id})


  (listar [_ q] (println "Listando pagamentos"))


  (atualizar
    [_ data]
    (println "Atualizando pagamento"))


  (remover
    [_ id]
    (println "Removendo pagamento")))


(defmethod repository/make-repository :pagamento
  [{:keys [connection]}]
  (tap> [::connection connection])
  (->PagamentoDatasource connection))
