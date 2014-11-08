(ns lazyreq.db.req
  (:use korma.core
        [korma.db :only (defdb)])
  (:require [lazyreq.db.schema :as schema]))

(defdb db schema/db-spec)

(defentity reqs)

(defn create-req [req]
  (insert reqs
          (values req)))

(defn update-req [req]
  (update reqs
  (set-fields req)
  (where {:rec_id (:rec_id req)})))

(defn get-req [id]
  (first (select reqs
                 (where {:rec_id id})
                 (limit 1))))

(defn find-one-req [req]
  (first (select reqs
                 (where req)
                 (limit 1))))

(defn list-unsuc-req []
  (select reqs (where {:status [not= 200]})))


(defn list-req []
  (select reqs))
