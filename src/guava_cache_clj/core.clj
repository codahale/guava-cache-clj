(ns guava-cache-clj.core
  (:require [clj-time.core :as t]
            [clojure.string :as string])
  (:import (java.util.concurrent TimeUnit)
           (clojure.lang IFn ILookup Counted)
           (com.google.common.base Supplier)
           (com.google.common.cache CacheBuilder CacheBuilderSpec CacheLoader
                                    LoadingCache RemovalCause RemovalListener
                                    RemovalNotification Weigher)
           (com.google.common.util.concurrent UncheckedExecutionException)))

(defn- cache-proxy
  [^LoadingCache cache]
  (reify
    IFn
    (invoke [this] this)
    (invoke [_ k] (try
                    (.getUnchecked cache k)
                    (catch UncheckedExecutionException e
                      (throw (.getCause e)))))
    (invoke [_ k not-found] (or (.getIfPresent cache k) not-found))
    (applyTo [this args] (clojure.lang.AFn/applyToHelper this args))

    ILookup
    (valAt [this k] (this k))
    (valAt [this k not-found] (this k not-found))

    Counted
    (count [_] (.size cache))

    Supplier
    (get [_] cache)))

(defn- removal-listener
  [f]
  (proxy [RemovalListener] []
    (onRemoval [^RemovalNotification n]
      (f {:key      (.getKey n)
          :value    (.getValue n)
          :evicted? (.wasEvicted n)
          :reason   (-> n .getCause .name string/lower-case keyword)}))))

(defn build
  ([loader-fn]
   (build loader-fn {}))
  ([loader-fn {:keys [concurrency-level disabled? expire-after-access
                      expire-after-write initial-capacity maximum-size
                      maximum-weight record-stats? refresh-after-write
                      removal-listener-fn soft-values? weak-keys? weak-values?
                      weight-fn]}]
   (let [b (if disabled?
             (CacheBuilder/from (CacheBuilderSpec/disableCaching))
             (CacheBuilder/newBuilder))]
     (when concurrency-level
       (.concurrencyLevel b concurrency-level))
     (when expire-after-access
       (.expireAfterAccess b (t/in-seconds expire-after-access)
                           TimeUnit/SECONDS))
     (when expire-after-write
       (.expireAfterWrite b (t/in-seconds expire-after-write)
                          TimeUnit/SECONDS))
     (when initial-capacity
       (.initialCapacity b initial-capacity))
     (when maximum-size
       (.maximumSize b maximum-size))
     (when maximum-weight
       (.maximumWeight b maximum-weight))
     (when record-stats?
       (.recordStats b))
     (when refresh-after-write
       (.refreshAfterWrite b (t/in-seconds refresh-after-write)
                           TimeUnit/SECONDS))
     (when removal-listener-fn
       (.removalListener b (removal-listener removal-listener-fn)))
     (when soft-values?
       (.softValues b))
     (when weak-keys?
       (.weakKeys b))
     (when weak-values?
       (.weakValues b))
     (when weight-fn
       (.weigher b (proxy [Weigher] []
                     (weigh [k v]
                       (weight-fn k v)))))
     (cache-proxy (.build b (proxy [CacheLoader] []
                              (load [k] (loader-fn k))))))))

(defn cache->map
  [^Supplier cache]
  (.asMap ^LoadingCache (.get cache)))

(defn invalidate!
  [^Supplier cache k]
  (.invalidate ^LoadingCache (.get cache) k))

(defn invalidate-all!
  [^Supplier cache]
  (.invalidateAll ^LoadingCache (.get cache)))

(defn stats
  [^Supplier cache]
  (let [s (.stats ^LoadingCache (.get cache))]
    {:average-load-penalty (.averageLoadPenalty s)
     :eviction-count       (.evictionCount s)
     :hit-count            (.hitCount s)
     :hit-rate             (.hitRate s)
     :load-couunt          (.loadCount s)
     :load-exception-count (.loadExceptionCount s)
     :load-exception-rate  (.loadExceptionRate s)
     :load-success-count   (.loadSuccessCount s)
     :miss-count           (.missCount s)
     :miss-rate            (.missRate s)
     :request-count        (.requestCount s)
     :total-load-time      (.totalLoadTime s)}))

(defn cleanup!
  [^Supplier cache]
  (.cleanUp ^LoadingCache (.get cache)))
