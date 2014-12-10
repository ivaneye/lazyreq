(ns lazyreq.errs.xmlrpc-err
  (:import (java.util Date))
  (:require [lazyreq.db.req :as db]
            [lazyreq.util.md5 :as md5]))

(set! *warn-on-reflection* true)

(defn- return-old-req [url header body remote-addr invoke_by]
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
                      :status 500
                      :pre_status 500
                      :invoke_by invoke_by
                      :add_time date
                      :update_time date})
      (do
        (db/update-req (assoc
                         (assoc old-req :update_time date)
                       :pre_status 500))
        (read-string (:response old-req))))))

(defn err [req e]
  (let [url (:next-url req)
        header (:headers req)
        body (:body req)
        remote-addr (get req :X-Real-IP (:remote-addr req))]
    (println req (.printStackTrace e))
    (return-old-req url header body remote-addr 1)))
