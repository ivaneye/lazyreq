(ns lazyreq.core.handler
  (:import (java.util Date))
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as str]
            [cronj.core :as c]
            [lazyreq.util.loader :refer :all]
            [clojure.java.io :as io]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn do-require [s]
  (if (vector? s)
    (doseq [p s]
      (require [(symbol p)]))
    (if s (require [(symbol s)]))))

(def mapping-cfg (:mappings cfg))

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

(defn resolve-core [cfg]
  (resolve (symbol (str (:core cfg) "/core"))))

(def resolve-core-mem (memoize resolve-core))

(defn resolve-cpre [cfg]
  (map #(resolve (symbol (str % "/pre"))) (:clojure-pres cfg)))

(def resolve-cpre-mem (memoize resolve-cpre))

(defn resolve-clj [s args k cfg]
  (map #(partial (resolve (symbol (str % s))) args) (k cfg)))

(def resolve-clj-mem (memoize resolve-clj))

(defn resolve-jpres [cfg]
  (map #(fn [x] (.pre (.newInstance (resolve (symbol %))) x)) (:java-pres cfg)))

(def resolve-jpres-mem (memoize resolve-jpres))

(defn resolve-jerrs [args cfg]
  (map #(partial (fn [a b] (.err (.newInstance (resolve (symbol %))) a b)) args) (:java-errs cfg)))

(def resolve-jerrs-mem (memoize resolve-jerrs))

(defn resolve-jposts [args cfg]
  (map #(partial (fn [a b] (.post (.newInstance (resolve (symbol %))) a b)) args) (:java-posts cfg)))

(def resolve-jposts-mem (memoize resolve-jposts))

(defn wrap-invoke [cfg args]
  (let [core (resolve-core-mem cfg)
        pres (resolve-cpre-mem cfg)
        errs (resolve-clj-mem "/err" args :clojure-errs cfg)
        jpres (resolve-jpres-mem cfg)
        jerrs (resolve-jerrs-mem args cfg)
        invoke-pres (concat pres jpres)
        invoke-errs (concat errs jerrs)]
    (try
      (let [param (invoke invoke-pres args)
            posts (resolve-clj-mem "/post" param :clojure-posts cfg)
            jposts (resolve-jposts-mem param cfg)
            inner-errs (resolve-clj-mem "/err" param :clojure-errs cfg)
            inner-jerrs (resolve-jerrs-mem param cfg)
            invoke-posts (concat posts jposts)
            invoke-inner-errs (concat inner-errs inner-jerrs)]
        (try (invoke invoke-posts (core param))
             (catch Exception e1
               (invoke invoke-inner-errs e1)))
        )
      (catch Exception e
        (invoke invoke-errs e)))))

;日终
(def cj (c/cronj :entries (vec (map #((resolve (symbol (str % "/task")))) (:tasks cfg)))))

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

(def app-routes (apply routes (map method mapping-cfg)))

(def app
  (wrap-defaults app-routes {}))
