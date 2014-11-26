(ns lazyreq.pres.format-header)


(defn pre
  "过滤无用的header"
  [req]
  (let [headers (:headers req)]
   (assoc req :headers  (-> headers
                         (dissoc "content-length")
                         (assoc :X-Requested-With "XMLHttpRequest")))))
