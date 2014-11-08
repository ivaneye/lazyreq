(ns lazyreq.pres.format-header)


(defn pre
  "过滤无用的header"
  [req]
  (assoc req :headers  (dissoc (:headers req) "content-length")))
