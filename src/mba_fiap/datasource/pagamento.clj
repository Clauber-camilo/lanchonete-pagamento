(ns mba-fiap.datasource.pagamento
  (:require
    [mba-fiap.repository.repository :as repository]
    [monger.collection :as mc]))


(defrecord PagamentoDatasource
  [db]

  repository/Repository

  (criar
    [_ data]
    (println "Criando pagamento"))


  (buscar
    [_ id]
    (println "Buscando pagamento")
    (let [insert (mc/save-and-return db "pagamento" {:id-pedido "4321"})]

      {:sucess true
       :id id
       :result (update insert :_id str)}))


  (listar [_ q] (println "Listando pagamentos"))


  (atualizar
    [_ data]
    (println "Atualizando pagamento"))


  (remover
    [_ id]
    (println "Removendo pagamento")))


(defmethod repository/make-repository :pagamento
  [{:keys [connection]}]
  (->PagamentoDatasource connection))
