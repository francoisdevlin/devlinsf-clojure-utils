(ns lib.sfd.date-utils
  (:use clojure.contrib.str-utils))

(def field-consts
     {:year java.util.Calendar/YEAR
      :month java.util.Calendar/MONTH
      :day java.util.Calendar/DAY_OF_MONTH
      :hour java.util.Calendar/HOUR
      :hour-of-day java.util.Calendar/HOUR_OF_DAY
      :minute java.util.Calendar/MINUTE
      :second java.util.Calendar/SECOND
      :ms java.util.Calendar/MILLISECOND
      :day-of-week java.util.Calendar/DAY_OF_WEEK})

(def value-consts
     {:jan java.util.Calendar/JANUARY
      :january java.util.Calendar/JANUARY
      :feb java.util.Calendar/FEBRUARY
      :february java.util.Calendar/FEBRUARY
      :mar java.util.Calendar/MARCH
      :march java.util.Calendar/MARCH
      :apr java.util.Calendar/APRIL
      :april java.util.Calendar/APRIL
      :may java.util.Calendar/MAY
      :jun java.util.Calendar/JUNE
      :june java.util.Calendar/JUNE
      :jul java.util.Calendar/JULY
      :july java.util.Calendar/JULY
      :aug java.util.Calendar/AUGUST
      :august java.util.Calendar/AUGUST
      :sep java.util.Calendar/SEPTEMBER
      :september java.util.Calendar/SEPTEMBER
      :oct java.util.Calendar/OCTOBER
      :october java.util.Calendar/OCTOBER
      :nov java.util.Calendar/NOVEMBER
      :november java.util.Calendar/NOVEMBER
      :dec java.util.Calendar/DECEMBER
      :december java.util.Calendar/DECEMBER})

(defn- month-replace
  [input-map]
  (let [month (input-map :month)]
    (if month
      (if (value-consts month)
	(assoc input-map :month (value-consts month))
	input-map)
      input-map)))

(defn to-ms-count-dispatch
  [& params]
  (let [lead-param (first params)]
    (cond
     (empty? params) ::empty
     (nil? lead-param) ::nil
     (instance? java.util.Calendar lead-param) ::calendar
     (map? lead-param) ::map
     true (class lead-param))))

(defmulti to-ms-count to-ms-count-dispatch)

(defmulti parse-date-string #((second %):format))

(defmethod parse-date-string :default
  [input-string & params]
  (let [default-options {:order [:month :day :year]}
	options (if (empty? params)
		  default-options
		    (merge default-options
		       (apply hash-map (reduce concat (partition 2 params)))))
	date-values (map
		     #(java.lang.Integer/parseInt %)
		     (re-split #"[-/]" input-string))
	base-map (apply hash-map (interleave (options :order) date-values))]
    (to-ms-count (assoc
		     base-map
		   :month (dec (base-map :month))
		   :year (if (< (base-map :year) 100)
			   (+ (base-map :year) 2000)
			   (base-map :year))))))

(defmethod to-ms-count ::empty
  [& params]
  (to-ms-count (java.util.Date. )))

(defmethod to-ms-count ::nil
  [& params]
  nil)

(defmethod to-ms-count java.lang.Long
  [& params]
  (first params))

(defmethod to-ms-count java.util.Date
  [& params]
  (. (first params) getTime))

(defmethod to-ms-count ::calendar
  [& params]
  (to-ms-count (. (first params) getTime)))

(defmethod to-ms-count ::map
  [& params]
  (to-ms-count (let [time-map (month-replace  (if (map? (first params))
					    (first params)
					    (apply hash-map (reduce concat (partition 2 params)))))
		 greg-cal (java.util.GregorianCalendar. )]
	     (do
	       (doseq [entry time-map]
		 (let [k (first entry)
		       v (second entry)]
		   (if (field-consts k)
		     (. greg-cal set (field-consts k) v))))
	       greg-cal))))

(defmethod to-ms-count :default
  [& params]
  (to-ms-count (apply hash-map params)))

(defmethod to-ms-count java.sql.Timestamp
  [& params]
  (. (first params) getTime))

(defmethod to-ms-count java.lang.String
  [& params]
  (apply parse-date-string (first params) (rest params)))

(defn long-time
  [& params]
  (apply to-ms-count params))

(defn date
  [& params]
  (let [temp-time (apply to-ms-count params)]
    (if temp-time
      (java.util.Date. temp-time))))

(defn greg-cal
  [& params]
  (let [temp-time (apply date params)]
    (if temp-time
      (let [temp-cal (java.util.GregorianCalendar.)]
	(do
	  (. temp-cal setTime temp-time)
	  temp-cal)))))

(defn sql-ts
  [& params]
    (let [temp-time (apply to-ms-count params)]
      (if temp-time
	(java.sql.Timestamp. temp-time))))

(defn time-map
  [& params]
  (let [temp-cal (apply greg-cal params)]
    (if temp-cal
      (apply merge (map 
		    #(hash-map (first %) (. temp-cal get (second %)))
		    field-consts)))))
    
(defn yesterday
  []
  (let [ms-per-day 86400000]
  (date (- (long-time) ms-per-day))))

(defn before
  "True if x is before y"
  [x y]
  (< (long-time x) (long-time y)))

(defn after
  "True if x is after y"
  [x y]
  (> (long-time x) (long-time y)))