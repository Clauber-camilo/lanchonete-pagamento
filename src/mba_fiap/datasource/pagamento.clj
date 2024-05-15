(ns mba-fiap.datasource.pagamento
  (:require
    [com.brunobonacci.mulog :as u]
    [mba-fiap.repository.repository :as repository]
    [monger.collection :as mc]))


(defn parse-result
  [result]
  (update result :_id str))


(defrecord PagamentoDatasource
  [db]

  repository/Repository

  (criar
    [_ data]
    (let [insert (mc/save-and-return db "pagamento" data)]

      (parse-result insert)))


  (buscar
    [_ oid]
    (let [pagamento (mc/find-map-by-id db "pagamento" oid)]
      pagamento))


  (listar
    [_ q]
    (let [pagamentos (mc/find-maps db "pagamento" q)
          _ (tap> pagamentos)]
      pagamentos))


  (atualizar
    [_ data]
    (let [id (:_id data)
          result (mc/update-by-id db "pagamento" id
                                  {:id-pedido (:id-pedido data)
                                   :status (:status data)})]
      result))


  (remover
    [_ id]
    (mc/remove-by-id db "pagamento" id)))


(defmethod repository/make-repository :pagamento
  [{:keys [connection]}]
  (->PagamentoDatasource connection))
