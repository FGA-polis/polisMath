;; Copyright 2012-present, Polis Technology Inc.
;; All rights reserved.
;; This source code is licensed under the BSD-style license found in the
;; LICENSE file in the root directory of this source tree. An additional grant
;; of patent rights for non-commercial use can be found in the PATENTS file
;; in the same directory.

(ns utils-test
  (:require [clojure.test :refer :all]
            [polismath.utils :refer :all]))


(deftest apply-kwargs-test
  (letfn [(fun [a b & {:keys [c d] :as kw-args}]
            {:a a :b b :c c :d d :kw-args kw-args})]
    (let [res (apply-kwargs fun "this" "that" {:c "crap" :d "stuff" :m "more"})]
      (testing "should pass through regular args"
        (is (= (res :a) "this"))
        (is (= (res :b) "that")))
      (testing "should pass through kw-args"
        (is (= (res :c) "crap"))
        (is (= (res :d) "stuff"))
        (is (= ((res :kw-args) :m) "more"))))))


