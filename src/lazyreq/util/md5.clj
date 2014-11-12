(ns lazyreq.util.md5
  (:import (java.security MessageDigest))
  (:require [clojure.string :as str]))

(defn- byte->string [b]
  (let [tmp (bit-and b 0xff)
        re (Long/toString tmp 16)]
    (if (< tmp 0x10) (str "0" re) re)))

(defn- bytes->hexstring [bytes]
  (str/upper-case (str/join (map byte->string bytes))))

(defn encode [s]
  (bytes->hexstring (.digest (MessageDigest/getInstance "MD5") (.getBytes s))))