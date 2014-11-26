(ns lazyreq.util.resolver
  (:require [clojure.string :as cstr]))

(defn resolve-charset [resp]
  (let [content-type ((:headers resp) "Content-Type")]
    (if (and content-type (.contains content-type "charset="))
      (-> content-type
          (cstr/split #";")
          last
          (cstr/split #"=")
          last))))


(defn resolve-resp [charset resp]
  (if charset (String. (.getBytes (str resp) charset) "UTF-8") (str resp)))
