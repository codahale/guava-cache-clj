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
    (is (= 1 (count cache)))

    (is (= [1] (vec (keys (cache->map cache)))))
    (is (= {1 1} (into {} (cache->map cache))))))

(deftest disabled-caching
  (let [cache (build identity {:disabled? true})]
    (is (= 1 (cache 1)))
    (is (= 2 (cache 2)))

    (is (= {} (cache->map cache)))))

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
