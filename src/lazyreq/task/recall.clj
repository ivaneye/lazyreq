(ns lazyreq.task.recall
  (:import (java.util Date))
  (:require [lazyreq.db.req :as db]
            [clj-http.client :as client]))

(defn- save-req [req result invoke_by]
      (db/update-req (assoc req :response (str result)
                                    :status (:status result)
                                    :update_time (Date.)
                                    :invoke_by invoke_by)))

(defn recall-xmlrpc [t opts]
  (println (str "Start" (:output opts)) ": " t)
  (let [unsuc-reqs (db/list-unsuc-req)]
    (doseq [req unsuc-reqs]
      (try (let [result (client/post (:url req)
                   {:headers (read-string (:header req))
                    :body    (:body req)})]
             (save-req req result 2))
           (catch Exception e
             (.printStackTrace e)))))
  (println (str "End" (:output opts)) ": " t))

(defn task []
  {:id "recall-xmlrpc"
   :handler recall-xmlrpc
   :schedule "0 /30 * * * * *"
   :opts {:output "Invoke recall-xmlrpc"}})
