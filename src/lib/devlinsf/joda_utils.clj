(ns lib.devlinsf.joda-utils
  (:use clojure.contrib.str-utils
	lib.devlinsf.date-utils)
  (:import (org.joda.time DateTime
			  Instant
			  Duration)))

(defmethod to-ms-count org.joda.time.DateTime
  [& params]
  (. (first params) getMillis))

(defmethod to-ms-count org.joda.time.base.BaseDateTime
  [& params]
  (. (first params) getMillis))

(defmethod to-ms-count org.joda.time.Instant
  [& params]
  (. (first params) getMillis))

(defn datetime
  [& params]
  (let [temp-time (apply to-ms-count params)]
    (if temp-time
      (org.joda.time.DateTime. temp-time))))

(defn instant
  [& params]
  (let [temp-time (apply to-ms-count params)]
    (if temp-time
      (org.joda.time.Instant. temp-time))))

(defn duration
  "Creates a Joda-Time Duration object"
  ([duration] (org.joda.time.Duration. (long duration)))
  ([start stop] (org.joda.time.Duration. (to-ms-count start) (to-ms-count stop))))

(defn add-dur
  "Adds a duration to the given time.  Currently assumes addition is communitive"
  ([input-time duration]
     (. (datetime input-time) plus duration))
  ([input-time duration & durations]
     (. (apply add-dur (datetime input-time) (first durations) (rest durations)) plus duration)))

(defn sub-dur
  "Adds a duration to the given time.  Currently assumes subtraction is communitive"
  ([input-time duration]
     (. (datetime input-time) minus duration))
  ([input-time duration & durations]
     (. (apply sub-dur (datetime input-time) (first durations) (rest durations)) minus duration)))
