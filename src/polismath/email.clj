;; Copyright 2012-present, Polis Technology Inc.
;; All rights reserved.
;; This source code is licensed under the BSD-style license found in the
;; LICENSE file in the root directory of this source tree. An additional grant
;; of patent rights for non-commercial use can be found in the PATENTS file
;; in the same directory.

(ns polismath.email
  (:require [polismath.env :as env]
            [clojure.tools.logging :as log]
            [clj-http.client :as client]))

(def ^:dynamic *mailgun-key* (:mailgun-api-key env/env))
(def ^:dynamic *mailgun-url* (:mailgun-url env/env))

(defn send-email!
  ([{:keys [from to subject text html] :as params}]
   (log/info "Sending email to:" to)
   (log/debug "  have mailgun key?" (boolean *mailgun-key*))
   (try
     (client/post *mailgun-url*
                  {:basic-auth ["api" *mailgun-key*]
                   :query-params params})
     (catch Exception e (.printStackTrace e))))
  ([from to subject text html] (send-email! {:from from :to to :subject subject :text text :html html}))
  ([from to subject text] (send-email! {:from from :to to :subject subject :text text})))

