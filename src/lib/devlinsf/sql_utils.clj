(ns lib.devlinsf.sql-utils
  (:use lib.devlinsf.str-utils
	lib.devlinsf.map-utils
	clojure.contrib.sql))

;;Connection Util
(defn connection-map
  [input-map]
  (let [con-map (dissoc input-map :db-host :db-port :db-name :db-vendor)
	drivers {:mysql {:classname "com.mysql.jdbc.Driver"
			 :subprotocol "mysql"}}]
    (merge con-map 
	   (hash-map 
	    :subname (str "//" (input-map :db-host)
			  ":"  (input-map :db-port)
			  "/"  (input-map :db-name)))
	   (drivers (input-map :db-vendor)))))

;;Query Utils
(defmulti sqlize-table class)
(defmulti sqlize-column #(if (nil? %)
			   ::nil
			    (class %)))
(defmulti clause-detect #(if (nil? %)
			   ::nil
			    (class %)))

(defn where-clause[where-map]
  (str-join " AND "
    (map #(str
            (sqlize-table (first %))
            (clause-detect (second %))
            (sqlize-column (second %)))
      where-map)))

(defn sql-select-str[table-name,where-map]
  (str "SELECT * FROM "
    (sqlize-table table-name)
    (if where-map
      (str " WHERE " (where-clause where-map)))))


(defmethod sqlize-table String [table-name]
  table-name)
(defmethod sqlize-table clojure.lang.Keyword [table-name]
  (str-rest (str table-name)))

(defn get-tuples
  [db table where-map]
  (with-connection db
    (with-query-results rs
      [(sql-select-str table where-map)]
      (loop [output ()
             results rs]
        (if (first results)
          (recur (conj output (first results)) (rest results))
          output)))))

(derive clojure.lang.LazilyPersistentVector ::in-clause)
(derive clojure.lang.PersistentVector ::in-clause)

(defmethod sqlize-column String [val]
  (str "\"" val "\""))
(defmethod sqlize-column :default [val]
    val)
(defmethod sqlize-column ::nil [val]
    "NULL")
(defmethod sqlize-column ::in-clause [val]
  (str "("(str-join ", " (map sqlize-column val)) ")"))

(defmethod clause-detect :default [val]
    "=")
(defmethod clause-detect ::in-clause [val]
  " IN ")
(defmethod clause-detect ::nil [val]
  " IS ")

(defmacro insert-entry-rails
  "A macro for inserting Rails"
  [table,value-map]
  (list 
   'clojure.contrib.sql/insert-values
   table
   (vec (concat '(:created_at :updated_at) (keys value-map)))
   (vec (concat (list 
		 (java.sql.Timestamp. (. (java.util.Date. ) getTime ))
		 (java.sql.Timestamp. (. (java.util.Date. ) getTime )))
		(map #(value-map %) (keys value-map))))))

;;;DDL Utils
(defn create-table-standard
  "Create a table to store blog entries"
  [table-name fields]
  (clojure.contrib.sql/create-table
   table-name
   [:id :integer "PRIMARY KEY" "AUTO_INCREMENT"]
   'fields))

(defmacro create-table-rails
  "A macro for creating Rails style tables"
  [table-name & fields]
  (concat `(
	    clojure.contrib.sql/create-table
	    ~table-name
	    [:id :integer "PRIMARY KEY" "AUTO_INCREMENT"]
	    [:created_at :datetime]
	    [:updated_at :datetime])
	  fields))