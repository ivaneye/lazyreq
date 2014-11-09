(ns lazyreq.core.handler
  (:import (java.util Date))
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as str]
            [cronj.core :as c]
            [lazyreq.util.loader :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def mapping-cfg (:mappings cfg))

(defn- do-require [s]
  (if (vector? s)
    (doseq [p s]
      (require [(symbol p)]))
    (require [(symbol s)])))

(doseq [c mapping-cfg]
  (do-require (:core c))
  (do-require (:clojure-pres c))
  (do-require (:clojure-errs c))
  (do-require (:clojure-posts c)))
(do-require (:tasks cfg))

(defn- invoke [funs args]
  (if (seq funs)
    (recur (rest funs) ((first funs) args))
    args))

(defn wrap-invoke [cfg args]
  (let [core (resolve (symbol (str (:core cfg) "/core")))
        pres (map #(resolve (symbol (str % "/pre"))) (:clojure-pres cfg))
        errs (map #(partial (resolve (symbol (str % "/err"))) args) (:clojure-errs cfg))
        jpres (map #(fn [x] (.pre (.newInstance (resolve (symbol %))) x)) (:java-pres cfg))
        jerrs (map #(partial (fn [a b] (.err (.newInstance (resolve (symbol %))) a b)) args) (:java-errs cfg))
        invoke-pres (concat pres jpres)
        invoke-errs (concat errs jerrs)]
    (try
      (let [param (invoke invoke-pres args)
            posts (map #(partial (resolve (symbol (str % "/post"))) param) (:clojure-posts cfg))
            jposts (map #(partial (fn [a b] (.post (.newInstance (resolve (symbol %))) a b)) param) (:java-posts cfg))
            inner-errs (map #(partial (resolve (symbol (str % "/err"))) param) (:clojure-errs cfg))
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
(def cj (c/cronj :entries (vec (map #(invoke-task (resolve (symbol (str % "/task")))) (:tasks cfg)))))

(defn recall []
  (c/start! cj))

(defmethod method
           "POST"
           [arg]
  (POST (:url arg) request
        (wrap-invoke arg request)))

(defmethod method
           "GET"
           [arg]
  (GET (:url arg) request
        (wrap-invoke arg request)))

(defmethod method
           "ANY"
           [arg]
  (ANY (:url arg) request
        (wrap-invoke arg request)))

(defmacro functionize [macro]
  `(fn [& args#] (eval (cons '~macro (cons 'app-routes args#)))))

(apply (functionize defroutes) (map method mapping-cfg))

(def app
  (wrap-defaults app-routes {}))
