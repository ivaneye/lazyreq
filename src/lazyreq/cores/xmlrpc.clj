(ns lazyreq.cores.xmlrpc
  (:require [clj-http.client :as client]))

(defn core
  "如果执行的为核心操作,则需要将其配置为执行funs的最后一个,并且返回执行结果.否则返回req"
  [req]
  (let [url (:next-url req)
        header (:headers req)
        body (:body req)]
    (client/post url
                 {:headers header
                  :body    body
                  :decompress-body false})))