(ns lazyreq.posts.save-resp
  (:import (java.util Date))
  (:require [lazyreq.db.req :as db]
            [lazyreq.util.md5 :as md5]))

(defn- save-req [url header body remote-addr result invoke_by]
  (let [old-req (db/find-one-req {:url_md5 (md5/encode url)
                                  :body_md5 (md5/encode body)})
        date (Date.)]
    (if (nil? old-req)
      (db/create-req {:from_ip remote-addr
                      :url url
                      :url_md5 (md5/encode url)
                      :header (str header)
                      :body body
                      :body_md5 (md5/encode body)
                      :response (str result)
                      :status (:status result)
                      :pre_status (:status result)
                      :invoke_by invoke_by
                      :add_time date
                      :update_time date})

      (db/update-req (assoc old-req :response (str result)
                                    :status (:status result)
                                    :pre_status (:status result)
                                    :update_time date
                                    :invoke_by invoke_by)))
    result))

(defn post
  "将返回的数据保存到数据库"
  [req resp]
  (let [url (:next-url req)
        header (:headers req)
        body (:body req)
        remote-addr (get req :X-Real-IP (:remote-addr req))]
    (save-req url header body remote-addr resp 1)))


