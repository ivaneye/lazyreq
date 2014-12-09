(ns lazyreq.util.md5-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [lazyreq.util.md5 :refer :all]))

(deftest test-app
  (testing "md5"
    (let [result (encode "123")]
      (is (= result "202CB962AC59075B964B07152D234B70")))))
