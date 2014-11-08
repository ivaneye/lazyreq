(ns lazyreq.pres.next-url
  (:require [clojure.string :as str]))

(defn pre
  "解析出需要跳转的url"
  [req]
  (assoc req :next-url (second (str/split (:query-string req) #"="))))
