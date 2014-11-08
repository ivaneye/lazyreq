(ns lazyreq.core.handler
  (:import (java.util Date))
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as str]
            [cronj.core :as c]
            [lazyreq.util.loader :as loader]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn- do-require [s]
  (println "======" s)
  (require [(symbol s)])
  s)

(defn- invoke [funs args]
  (if (seq funs)
    (recur (rest funs) ((first funs) args))
    args))

(defn wrap-invoke [cfg args]
  (let [core (resolve (symbol (str (do-require (:core cfg)) "/core")))
        pres (map #(resolve (symbol (str (do-require %) "/pre"))) (:clojure-pres cfg))
        errs (map #(partial (resolve (symbol (str (do-require %) "/err"))) args) (:clojure-errs cfg))
        jpres (map #(fn [x] (.pre (.newInstance (resolve (symbol %))) x)) (:java-pres cfg))
        jerrs (map #(partial (fn [a b] (.err (.newInstance (resolve (symbol %))) a b)) args) (:java-errs cfg))
        invoke-pres (concat pres jpres)
        invoke-errs (concat errs jerrs)]
    (try
      (let [param (invoke invoke-pres args)
            posts (map #(partial (resolve (symbol (str (do-require %) "/post"))) param) (:clojure-posts cfg))
            jposts (map #(partial (fn [a b] (.post (.newInstance (resolve (symbol %))) a b)) param) (:java-posts cfg))
            inner-errs (map #(partial (resolve (symbol (str (do-require %) "/err"))) param) (:clojure-errs cfg))
            inner-jerrs (map #(partial (fn [a b] (.err (.newInstance (resolve (symbol %))) a b)) param) (:java-errs cfg))
            invoke-posts (concat posts jposts)
            invoke-inner-errs (concat inner-errs inner-jerrs)]
        (try (invoke invoke-posts (core param))
             (catch Exception e1
               (invoke invoke-inner-errs e1)))
        )
      (catch Exception e
        (invoke invoke-errs e)))))


(defn invoke-task [fun]
  (fun))

;日终
(def cj (c/cronj :entries (vec (map #(invoke-task (resolve (symbol (str (do-require %) "/task")))) (:tasks loader/cfg)))))

(defn recall []
  (c/start! cj))

;(defmacro route [mp]
;   `(~(:method mp) ~(:url mp) ~'request
;        (wrap-invoke ~mp ~'request)))
;
;(defmacro defreqmap [& mps]
;  `(defroutes app-routes
;       ~@(map #(route %) mps)))
;
;(defreqmap {:url           "/xmlrpc"
;            :method     POST
;            :clojure-pres  ["lazyreq.pres.resolve-body" "lazyreq.pres.format-header" "lazyreq.pres.next-url"]
;            :clojure-errs  ["lazyreq.errs.xmlrpc-err"]
;            :clojure-posts ["lazyreq.posts.ungzip" "lazyreq.posts.save-resp"]
;            :core          "lazyreq.cores.xmlrpc"})

(defroutes app-routes
  (ANY "/xmlrpc" request
       (wrap-invoke loader/mapping-cfg request))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes {}))
