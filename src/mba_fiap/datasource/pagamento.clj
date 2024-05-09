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
    [_ id]
    (let [pagamento (mc/find-one-as-map db "pagamento" {:id-pedido id})]
      pagamento))


  (listar
    [_ q]
    (let [pagamentos (mc/find-maps db "pagamento" q)
          _ (tap> pagamentos)]
      pagamentos))


  (atualizar
    [_ data]
    (let [{:keys [id-pedido]} data
          pagamento (mc/find-one-as-map db "pagamento" {:id-pedido id-pedido})
          _ (tap> pagamento)]
      (if (nil? pagamento)
        (u/log ::pagamento_datasource, :msg "Pagamento não encontrado")
        (let [id (:_id pagamento)
              result (mc/update-by-id db "pagamento" id
                                      {:id-pedido (:id-pedido pagamento)
                                       :status (:status data)})]
          result))))


  (remover
    [_ id]
    (println "Removendo pagamento")))


(defmethod repository/make-repository :pagamento
  [{:keys [connection]}]
  (->PagamentoDatasource connection))
