(ns lazyreq.errs.xmlrpc-err
  (:import (java.util Date))
  (:require [lazyreq.db.req :as db]))


(defn- return-old-req [url header body remote-addr invoke_by]
  (let [old-req (db/find-one-req {:url url
                                  :body body})
        date (Date.)]
    (if (nil? old-req)
      (db/create-req {:from_ip remote-addr
                      :url url
                      :header (str header)
                      :body body
                      :status 500
                      :invoke_by invoke_by
                      :add_time date
                      :update_time date})
      (if (= 200 (:status old-req))
        (read-string (:response old-req))
        (db/update-req (assoc old-req :update_time date))
        ))))

(defn err [req e]
  (let [url (:next-url req)
        header (:headers req)
        body (:body req)]
    (.printStackTrace e)
    (return-old-req url header body (:remote-addr req) 1)))
