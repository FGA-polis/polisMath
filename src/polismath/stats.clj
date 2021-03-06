;; Copyright 2012-present, Polis Technology Inc.
;; All rights reserved.
;; This source code is licensed under the BSD-style license found in the
;; LICENSE file in the root directory of this source tree. An additional grant
;; of patent rights for non-commercial use can be found in the PATENTS file
;; in the same directory.

(ns polismath.stats
  (:use clojure.core.matrix)
  (:require [plumbing.core :as pc]
            [clojure.tools.trace :as tr]))


(defn prop-test
  [succ n]
  (let [[succ n] (map inc [succ n])]
    (* 2
       (sqrt n)
       (+ (/ succ n) -0.5))))


(defn two-prop-test
  [succ-in succ-out pop-in pop-out]
  (let [[succ-in succ-out pop-in pop-out]
          (map inc [succ-in succ-out pop-in pop-out])
        pi1 (/ succ-in pop-in)
        pi2 (/ succ-out pop-out)
        pi-hat (/ (+ succ-in succ-out) (+ pop-in pop-out))]
    (if (= pi-hat 1)
      ; XXX - this isn't quite right... could actually solve this using limits. I think there is some theorem
      ; that lets you take the ratio of the derivatives or something...
      0
      (/ (- pi1 pi2)
         (sqrt
           (* pi-hat
              (- 1 pi-hat)
              (+ (/ 1 pop-in) (/ 1 pop-out))))))))


(defn z-sig-90?
  "Test whether z-statistic is significant at %90 confidence with alternative hypothesis >"
  [z-val]
  (> z-val 1.2816))


(defn z-sig-95?
  "Test whether z-statistic is significant at %95 confidence with alternative hypothesis >"
  [z-val]
  (> z-val 1.6449))


