(ns lazyreq.task.recall
  (:import (java.util Date))
  (:require [lazyreq.db.req :as db]
            [taoensso.timbre :as timbre :refer [log debug info error with-log-level with-logging-config]]
            [clj-http.client :as client]))

(timbre/refer-timbre)

(defn- save-req [req result invoke_by]
      (db/update-req (assoc req :response (str result)
                                    :status (:status result)
                                    :pre_status (:status result)
                                    :update_time (Date.)
                                    :invoke_by invoke_by)))

(defn recall-xmlrpc [t opts]
  (info "recall Start" (:output opts) ": " t)
  (let [unsuc-reqs (db/list-unsuc-req)]
    (doseq [req unsuc-reqs]
      (try (let [result (client/post (:url req)
                   {:headers (read-string (:header req))
                    :body    (:body req)
                    :decompress-body false})]
             (save-req req result 2))
           (catch Exception e
             (.printStackTrace e)))))
  (info "recall End" (:output opts) ": " t))

(def task
  {:id "recall-xmlrpc"
   :handler recall-xmlrpc
   :schedule "0 /30 * * * * *"
   :opts {:output "Invoke recall-xmlrpc"}})
