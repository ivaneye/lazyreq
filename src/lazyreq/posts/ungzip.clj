(ns lazyreq.posts.ungzip)


(defn post
  "服务器可能进行gzip压缩,需重设content-length"
  [req resp]
  (let [body-length (count (:body resp))]
     (assoc resp :headers (assoc (:headers resp) "Content-Length" (str body-length)))))
