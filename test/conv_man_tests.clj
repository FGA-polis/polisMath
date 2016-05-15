;; Copyright 2012-present, Polis Technology Inc.
;; All rights reserved.
;; This source code is licensed under the BSD-style license found in the
;; LICENSE file in the root directory of this source tree. An additional grant
;; of patent rights for non-commercial use can be found in the PATENTS file
;; in the same directory.

(ns conv-man-tests
  (:use polismath.utils
        test-helpers)
  (:require [clojure.test :refer :all]
            [polismath.conv-man :refer :all]))


(deftest get-or-set-test
  (testing "should work when not set"
    (let [a (atom {})]
      (get-or-set! a :shit (fn [] "stuff"))
      (is (= (:shit @a) "stuff"))))
  (testing "should work when set"
    (let [a (atom {:shit "stuff"})]
      (is (= (get-or-set! a :shit (fn [] "crap"))
             "stuff")))))


; Initialization test
(deftest split-batches-test
  (testing "of a missing param"
    (let [messages [[:votes [{:a 4} {:b 5} {:c 6}]]
                    [:votes [{:d 7} {:e 8} {:f 9}]]]]
      (is (= (split-batches messages)
             {:votes [{:a 4} {:b 5} {:c 6} {:d 7} {:e 8} {:f 9}]}))
      (is (nil? (:moderation (split-batches messages))))))
  (testing "of one message"
    (let [messages [[:votes [{:a 4} {:b 5} {:c 6}]]]]
      (is (= (split-batches messages)
             {:votes [{:a 4} {:b 5} {:c 6}]}))))
  (testing "of combined messages"
    (let [messages [[:votes [{:a 4} {:b 5} {:c 6}]]
                    [:votes [{:d 7} {:e 8} {:f 9}]]
                    [:moderation [{:g 4} {:h 5} {:i 6}]]
                    [:moderation [{:j 7} {:k 8} {:l 9}]]]]
      (is (= (split-batches messages)
             {:votes [{:a 4} {:b 5} {:c 6} {:d 7} {:e 8} {:f 9}]
              :moderation [{:g 4} {:h 5} {:i 6} {:j 7} {:k 8} {:l 9}]})))))


