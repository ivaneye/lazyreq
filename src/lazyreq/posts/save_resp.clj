(ns lazyreq.posts.save-resp
  (:import (java.util Date))
  (:require [lazyreq.db.req :as db]))

(defn- save-req [url header body remote-addr result invoke_by]
  (let [old-req (db/find-one-req {:url url
                                  :body body})
        date (Date.)]
    (if (nil? old-req)
      (db/create-req {:from_ip remote-addr
                      :url url
                      :header (str header)
                      :body body
                      :response (str result)
                      :status (:status result)
                      :pre_status (:status result)
                      :invoke_by invoke_by
                      :add_time date
                      :update_time date})

      (db/update-req (assoc old-req :response (str result)
                                    :status (:status result)
                                    :pre_status (:stack result)
                                    :update_time date
                                    :invoke_by invoke_by)))
    result))

(defn post
  "将返回的数据保存到数据库"
  [req resp]
  (let [url (:next-url req)
        header (:headers req)
        body (:body req)
        remote-addr (:remote-addr req)]
    (save-req url header body remote-addr resp 1)))
