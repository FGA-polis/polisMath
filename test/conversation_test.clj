;; Copyright 2012-present, Polis Technology Inc.
;; All rights reserved.
;; This source code is licensed under the BSD-style license found in the
;; LICENSE file in the root directory of this source tree. An additional grant
;; of patent rights for non-commercial use can be found in the PATENTS file
;; in the same directory.

(ns conversation-test
  (:require [clojure.test :refer :all]
            [polismath.conversation :refer :all]
            [polismath.named-matrix :refer :all]
            [clojure.tools.trace :as tr]))


(def rat-mat
  (named-matrix [:a :b] [:x :y] [[1 0] [-1 1]]))


(let [empty-conv {:rating-mat (named-matrix)}
      small-conv {:rating-mat rat-mat}

      single-vote   [{:created 500 :pid :a, :tid :x, :vote 3}]
      wonky-votes   [{:created 600 :pid :b, :tid :x, :vote 0}
                     {:created 700 :pid :a, :tid :y, :vote 1}]
      several-votes (concat single-vote wonky-votes)
      full-votes    (for [pid [:a :b :c] tid [:x :y :z]]
                     {:created 100 :pid pid :tid tid :vote (rand)})

      big-conv {:rating-mat
                  (named-matrix
                    [:p1 :p2 :p3 :p4 :p5]
                    [:c1 :c2 :c3 :c4]
                    [[ 0  1  0 -1  1]
                     [-1  0 -1 -1  0]
                     [ 1  0  1  0  1]
                     [ 1 -1  0 -1  0]
                     [-1 -1  0  1  0]])
                :pca {:center [0.0 -0.2 -0.0  0.4] ;actually calculated this...
                      :comps [[0.4  0.2 -0.3  0.7] ;this is faked...
                              [0.1 -0.5  0.2  0.2]]}}]

  (deftest init-conv-update-test
    (testing "empty matrix and one vote"
      (is (conv-update empty-conv single-vote)))
    (testing "empty matrix and no vote"
      (is (conv-update empty-conv [])))
    (testing "empty matrix and several votes"
      (is (conv-update empty-conv several-votes)))
    (testing "empty matrix and full votes"
      (is (conv-update empty-conv full-votes)))
    (testing "empty matrix and wonky votes"
      (is (conv-update empty-conv wonky-votes))))

  ; Note that this assumes that a small conversation will actually use the small-conv-update implementation
  (deftest small-conv-update-test
    (testing "small matrix and one vote"
      (is (conv-update small-conv single-vote)))
    (testing "small matrix and no vote"
      (is (conv-update small-conv [])))
    (testing "small matrix and several votes"
      (is (conv-update small-conv several-votes)))
    (testing "small matrix and full votes"
      (is (conv-update small-conv full-votes)))
    (testing "small matrix and wonky votes"
      (is (conv-update small-conv wonky-votes))))


  (let [votes-base-fnk (:votes-base small-conv-update-graph)
        group-votes-fn (fn [conv]
                         ((:group-votes small-conv-update-graph)
                           (assoc conv :votes-base (votes-base-fnk conv))))

        group-clusters [{:id :g1 :members [:b1 :b2]} {:id :g2 :members [:b3 :b4 :b5]}]
        base-clusters (mapv (fn [[id m]] {:id id :members [m]})
                            [[:b1 :p1] [:b2 :p2] [:b3 :p3] [:b4 :p4] [:b5 :p5]])

        conv (assoc big-conv
                    :group-clusters group-clusters
                    :base-clusters base-clusters)
        conv (assoc conv
                    :bid-to-pid ((:bid-to-pid small-conv-update-graph) conv))]

    (deftest vote-base-test
      (letfn [(get-count [tid vote bid]
                (-> conv votes-base-fnk tid vote (nth bid)))]

        (testing "agree counts"
          (doseq [pid [1 4]]
            (is (= (get-count :c1 :A pid)
                   1)))
          (doseq [pid [0 2 3]]
            (is (= (get-count :c1 :A pid)
                   0))))

        (testing "disagree counts"
          (is (= (get-count :c1 :D 2)
                 1))
          (doseq [pid [0 1]]
            (is (= (get-count :c1 :D pid)
                   0))))

        (testing "disagree counts"
          (doseq [pid [0 1]]
            (is (= (get-count :c1 :S pid)
                   1))))))

    (deftest group-votes-test
      (letfn [(get-count [gid tid vote]
                (-> conv group-votes-fn gid :votes tid vote))]
        (testing "members counts"
          (is (= (-> conv group-votes-fn :g1 :n-members) 2))
          (is (= (-> conv group-votes-fn :g2 :n-members) 3)))
        (testing "aggrees"
          (is (= (get-count :g1 :c1 :A) 1))
          (is (= (get-count :g2 :c1 :A) 1)))
        (testing "aggrees"
          (is (= (get-count :g1 :c1 :D) 0))
          (is (= (get-count :g2 :c1 :D) 2))))))


  ; Test that iterating on previous pca/clustering results makes sense
  (let [fleshed-conv (conv-update small-conv single-vote)]
    (deftest small-conv-iterative-test
      (testing "fleshed conv and full matrix"
        (is (conv-update fleshed-conv full-votes)))
      (testing "fleshed conv and wonky vote"
        (is (conv-update fleshed-conv [{:created 999 :pid :j :tid :k :vote -1}]))))
  
    (deftest moderation-test
      (testing "from scratch with fleshed out conv"
        (is (= (:mod-out (mod-update fleshed-conv
                                     [{:tid :x :mod -1 :modified 1234}]))
               (set [:x]))))
      (testing "based on previous moderations"
        (is (= (:mod-out (mod-update (assoc big-conv :mod-out [:x])
                                     [{:tid :y :mod -1 :modified 1856}]))
               (set [:x :y])))))
      (testing "with only approve mods"
        (is (= (:mod-out (mod-update big-conv [{:tid :x :mod 1 :modified 8576}
                                               {:tid :y :mod 1 :modified 9856}]))
               #{})))
      (testing "with only approve mods"
        (is (= (:mod-out (mod-update big-conv [{:tid :x :mod 1 :modified 7654}
                                               {:tid :y :mod -1 :modified 8567}]))
               #{:y}))))

  (deftest large-conv-update-test
      (testing "should work with votes for only existing ptpts/cmts"
        (is (large-conv-update {:conv big-conv :opts {} 
                                :votes [{:created 100 :pid :p1 :tid :c1 :vote  1}
                                        {:created 200 :pid :p3 :tid :c3 :vote -1}]})))
      (testing "should work with votes for new cmts"
        (is (large-conv-update {:conv big-conv :opts {}
                                :votes [{:created 100 :pid :p3 :tid :c5 :vote  1}
                                        {:created 200 :pid :p5 :tid :c5 :vote -1}]})))))


