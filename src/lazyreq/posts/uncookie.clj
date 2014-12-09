(ns lazyreq.posts.uncookie)


(defn post
  "去除cookie"
  [req resp]
  (dissoc resp :cookies))
