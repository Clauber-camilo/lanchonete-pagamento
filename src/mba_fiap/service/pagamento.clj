(ns mba-fiap.service.pagamento
  (:require
    [mba-fiap.base.validation :as validation]
    [mba-fiap.model.pagamento :as pagamento])
  (:import
    (mba_fiap.repository.repository
      Repository)))


(defn pagamento->response
  [pagamento]
  (let [[{:pagamento/keys [id id_pedido total status created_at]}] pagamento]
    {:id id
     :id-pedido id_pedido
     :total total
     :status status
     :created-at created_at}))


(defn criar-pagamento
  [^Repository repository pagamento]
  (let [pagamento (.criar repository pagamento)
        response (pagamento->response pagamento)]
    response))


(defn atualizar-status-pagamento
  [^Repository repository id-pedido status]
  {:pre [(instance? Repository repository)
         (uuid? id-pedido)
         (validation/schema-check pagamento/Pagamento status)]}
  (let [data {:id-pedido id-pedido :status status}
        pagamento (.atualizar repository data)
        response (pagamento->response pagamento)]

    response))


(defn buscar-por-id-pedido
  [^Repository repository id-pedido]
  {:pre [(instance? Repository repository)]}
  (let [pagamento (.buscar repository id-pedido)]
    (tap> [::service pagamento])
    (if (empty? pagamento)
      {:error "Pagamento não encontrado"}
      pagamento)))
