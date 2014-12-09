(ns lazyreq.core.core
  (:import (java.util Date))
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as str]
            [cronj.core :as c]
            [lazyreq.task.recall :as recall]
            [lazyreq.pres.resolve-body :as resolve-body]
            [lazyreq.pres.format-header :as format-header]
            [lazyreq.pres.next-url :as next-url]
            [lazyreq.posts.save-resp :as save-resp]
            [lazyreq.posts.uncookie :as uncookie]
            [lazyreq.cores.xmlrpc :as xmlrpc]
            [lazyreq.errs.xmlrpc-err :as xmlrpc-err]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn call-xmlrpc
  "调用rpc核心逻辑方法,分别调用pre方法->core方法->post方法来执行逻辑"
  [req]
    (try
      (let [new-req (->> req resolve-body/pre format-header/pre next-url/pre)]
        (try
          (save-resp/post new-req (uncookie/post new-req (xmlrpc/core new-req)))
             (catch Exception e1
               (xmlrpc-err/err new-req e1))))
      (catch Exception e
        (println req (.printStackTrace e)))))

;日终
(def cj (c/cronj :entries [recall/task]))

(defn recall []
  (c/start! cj))


(defroutes app-routes
  (POST "/xmlrpc" request (call-xmlrpc request))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes {}))
