;; Copyright 2012-present, Polis Technology Inc.
;; All rights reserved.
;; This source code is licensed under the BSD-style license found in the
;; LICENSE file in the root directory of this source tree. An additional grant
;; of patent rights for non-commercial use can be found in the PATENTS file
;; in the same directory.

(ns polismath.env
  (:require [environ.core :as environ]))

(def ^:dynamic env environ/env)

(defmacro with-env-overrides
  [overrides & body]
  `(binding [env (merge env ~overrides)]
     ~@body))

