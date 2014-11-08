(ns lazyreq.pres.resolve-body)


(defn pre
  "对于request的body来说,只能读取一次.这里将body由stream转化为string,方便多次获取"
  [req]
  (assoc req :body (slurp (:body req))))
