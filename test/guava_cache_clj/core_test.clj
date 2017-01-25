(ns guava-cache-clj.core-test
  (:require [clj-time.core :as t]
            [clojure.test :refer :all]
            [guava-cache-clj.core :refer :all]))

(deftest basic-caching
  (let [cache (build identity)]
    ;; IFn
    (is (= 1 (cache 1)))
    (is (= 300 (cache 2 300)))

    ;; ILookup
    (is (= 1 (get cache 1)))
    (is (= 300 (get cache 2 300)))

    ;; Counted
    (is (= 1 (count cache)))))

(deftest cache->map-test
  (let [cache (build identity)]
    (is (= 1 (cache 1)))
    (is (= [1] (vec (keys (cache->map cache)))))
    (is (= {1 1} (into {} (cache->map cache))))))

(deftest invalidation-test
  (let [cache (build identity)]
    (is (= 1 (cache 1)))
    (is (= 2 (get cache 2)))
    (invalidate! cache 1)
    (is (= {2 2} (into {} (cache->map cache))))
    (invalidate-all! cache)
    (is (= {} (into {} (cache->map cache))))))

(deftest cleanup!-test
  (let [cache (build identity)]
    ;; impossible to test, unfortunately
    (cleanup! cache)))

(deftest disabled-caching
  (let [cache (build identity {:disabled? true})]
    (is (= 1 (cache 1)))
    (is (= 2 (cache 2)))

    (is (= {} (cache->map cache)))))

(deftest removal-listener-test
  (let [n (atom [])
        cache (build identity {:removal-listener-fn #(swap! n conj %)
                               :maximum-size 1})]
    (is (= 1 (cache 1)))
    (is (= 2 (cache 2)))
    (is (= [{:key 1, :value 1, :evicted? true, :reason :size}] @n))))

(deftest weight-test
  (let [cache (build identity {:maximum-weight 50
                               :weight-fn      (fn [x _] x)})]
    (is (= 1 (cache 1)))
    (is (= 2 (cache 2)))
    (is (= 50 (cache 50)))
    (is (= {1 1, 2 2} (into {} (cache->map cache))))))

(deftest stats-test
  (let [cache (build identity {:record-stats? true})]
    (is (= {:eviction-count       0
            :load-success-count   0
            :miss-count           0
            :load-couunt          0
            :average-load-penalty 0.0
            :miss-rate            0.0
            :total-load-time      0
            :hit-rate             1.0
            :hit-count            0
            :load-exception-rate  0.0
            :request-count        0
            :load-exception-count 0}
           (stats cache)))))

(deftest build-options
  (build identity {:concurrency-level 20})
  (build identity {:expire-after-access (t/minutes 5)})
  (build identity {:expire-after-write (t/minutes 5)})
  (build identity {:initial-capacity 20})
  (build identity {:maximum-size 20})
  (build identity {:maximum-weight 40
                   :weight-fn      #(* 20 (count %))})
  (build identity {:record-stats? true})
  (build identity {:refresh-after-write (t/minutes 5)})
  (build identity {:removal-listener-fn #(prn %)})
  (build identity {:soft-values? true})
  (build identity {:weak-keys? true})
  (build identity {:weak-values? true}))
