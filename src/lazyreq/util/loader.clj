(ns lazyreq.util.loader
  (:import (java.io PushbackReader))
  (:require [clojure.java.io :as io]))

(defn load-config [filename]
  (with-open [r (io/reader (io/resource filename))]
    (read (PushbackReader. r))))


(def cfg (load-config "config.clj"))
(def mapping-cfg (:mappings cfg))
(def db-cfg (load-config "database.clj"))