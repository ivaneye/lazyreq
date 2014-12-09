(ns lazyreq.util.loader
  (:import (java.io PushbackReader))
  (:require [clojure.java.io :as io]))

(defn- argument-list->argument-map
  [args]
  (let [keys (map first (partition 2 args))
        unique-keys (set keys)]
    (if (= (count keys) (count unique-keys))
      (apply hash-map args)
      (let [duplicates (->> (frequencies keys)
                            (remove #(> 2 (val %)))
                            (map first))]
        (throw
          (IllegalArgumentException.
            (format "Duplicate keys: %s"
                    (clojure.string/join ", " duplicates))))))))

(defmacro defdb
  "定义数据库配置"
  [db-name & args]
  `(let [args# ~(argument-list->argument-map args)]
     (def ~'db-cfg args#)))

(defmacro defcfg
  "定义url请求配置"
  [cfg-name & args]
  `(let [args# ~(argument-list->argument-map args)]
     (def ~'cfg args#)))

(defn load-config [file]
(binding [*ns* (find-ns 'lazyreq.util.loader)]
  (try (load-file (.getAbsolutePath (io/file (io/resource file))))
       (catch Exception e
         (throw (Exception. (format "Error loading %s" file) e))))))

(defmulti method (fn [arg] (:method arg)))

(load-config "database.clj")
(load-config "config.clj")
