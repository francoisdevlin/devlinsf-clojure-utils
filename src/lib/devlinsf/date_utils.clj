(ns lib.devlinsf.date-utils
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

(defn- create-date
  "Can set year, month, day by keywords"
  [& params]
  (let [time-map (month-replace  (if (map? (first params))
				   (first params)
				   (apply hash-map (reduce concat (partition 2 params)))))
        greg-cal (java.util.GregorianCalendar. )]
    (do
      (doseq [entry time-map]
        (let [k (first entry)
              v (second entry)]
            (if (field-consts k)
                (. greg-cal set (field-consts k) v))))
    (. greg-cal getTime))))

(defn parse-date
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
    (create-date (assoc
		     base-map
		   :month (dec (base-map :month))
		   :year (if (< (base-map :year) 100)
			   (+ (base-map :year) 2000)
			   (base-map :year))))))

(defn date
  [& params]
  (let [lead-param (first params)]
    (cond
     (empty? params) (java.util.Date. )
     (nil? lead-param) nil
     (= (class lead-param) java.util.Date) (java.util.Date. (. lead-param getTime))
     (= (class lead-param) java.lang.Long) (java.util.Date. lead-param)
     (= (class lead-param) java.sql.Timestamp) (java.util.Date. (. lead-param getTime))
     (= (class lead-param) java.lang.String) (apply parse-date lead-param (rest params))
     (instance? java.util.Calendar lead-param) (java.util.Date. (. (. lead-param getTime) getTime))
     (map? lead-param) (create-date lead-param)
     true (apply create-date params))))

(defn long-time
  [& params]
  (if (= (class (first params)) java.lang.Long)
    (first params)
    (let [temp-date (apply date params)]
      (if temp-date
	(. temp-date getTime)))))

(defn greg-cal
  [& params]
  (if (= (class (first params)) java.util.GregorianCalendar)
    (first params)
    (let [temp-time (apply long-time params)]
      (if temp-time
	(let [temp-cal (java.util.GregorianCalendar.)]
	  (do
	    (. temp-cal setTime (java.util.Date. temp-time))
	    temp-cal))))))

(defn sql-ts
  [& params]
  (if (= (class (first params)) java.sql.Timestamp)
    (first params)  
    (let [temp-time (apply long-time params)]
      (if temp-time
	(java.sql.Timestamp. temp-time)))))

(defn cal-get
  "This is a wrapper for the java.util.Calendar get method."
  [field & params]
  (let [java-field (if (keyword? field)
		     (field-consts field)
		     field)]
    (. (apply greg-cal params) get java-field)))

(defn time-map
  [& params]
  (let [temp-cal (apply greg-cal params)]
    (apply merge (map #(hash-map (first %) (. temp-cal get (second %))) field-consts))))
    