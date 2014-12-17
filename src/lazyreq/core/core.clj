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
            [ring.util.response :refer [charset]]
            [taoensso.timbre :as timbre :refer [log debug info error with-log-level with-logging-config]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(timbre/refer-timbre)

(defn call-xmlrpc
  "调用rpc核心逻辑方法,分别调用pre方法->core方法->post方法来执行逻辑"
  [req]
  (info "call-xmlrpc START and uri = " (:uri req))
    (try
      (let [new-req (->> req resolve-body/pre format-header/pre next-url/pre)]
        (try
          (let [result (save-resp/post new-req (uncookie/post new-req (xmlrpc/core new-req)))]
            (info "call-xmlrpc END and uri = " (:uri req))
              (charset result "UTF-8"))
             (catch Exception e1
               (error "call-xmlrpc CORE/POST ERR and uri = " (:uri req))
               (error (.printStackTrace e1))
               (charset (xmlrpc-err/err new-req e1) "UTF-8"))))
      (catch Exception e
        (error "call-xmlrpc PRE ERR and uri = " (:uri req))
        (error (.printStackTrace e)))))

;日终
(def cj (c/cronj :entries [recall/task]))

(defn recall []
  (c/start! cj))


(defroutes app-routes
  (POST "/xmlrpc" request (call-xmlrpc request))
  (route/not-found (fn [req] (info "uri \"" (:uri req) "\" not found") "Not Found")))

(def app
  (wrap-defaults app-routes {}))
