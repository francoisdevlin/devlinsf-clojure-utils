#SQL-Utilities

Sean Devlin 

June 10, 2009

This namespace depends on `clojure.contrib.sql`, as well as `lib.devlinsf.str-utils`.  Also, you will need to have the appropriate JDBC driver installed in your classpath.

#`connection-map`

This is a utility method for generating the connection map c.c.sql expects.  

##:db-vendor

Currently only MySQL is supported.  Pass the keyword `:mysql`

##:db-host

This is the IP address of the database server.  Check with your DBA for local settings.

##:db-port

Check with your DBA for local settings.  MySQL defaults to 3306 and Postgres 5432.

##:db-name

This is the name of the database you're trying to connect to.

##:username

Your username

##:password

Your password

#sql-select-str
This is a wrapper for generating SQL
###Signature
	(sql-select-str table column-vector where-map)
	
Table can be either a string or keyword.

Column vector controls which columns are retrieved.  If it is empty or nil, `SELECT *` is generated.

The where-map is used to create a where clause.

Joins are deliberately not supported.
###Usage
	user=> (sql-select-str :foo [:a :b] {:a "Awesome" :b [1 2]})
	"SELECT a, b FROM foo WHERE a=\"Awesome\" AND b IN (1, 2)"

##`where-clause [conditions-map]`

The goal was to have the following s-expression generate valid SQL

	(str "select * from FOO where " (where-clause _conditions-map_) )

This function takes a map as inputs, and expands it to a SQL where clause.  This is probably best explained by example.


### Substitution Examples

First, consider the most basic condition you could think of.

    (where-clause {:id 1}) => "id=1"

Substituting into our SQL builder above yeilds

    "select * from FOO where id=1"

Got it?  Good. The map can also have string keys.

    (where-clause {"id" 1}) => "id=1"

Also, a key with a value of nil will generate an "IS NULL" clause

	(where-clause {"id" nil}) => "id IS NULL"

### AND, OR & IN

I have found the following statements to be true for 99% of my queries.

* AND is used to restrict multiple columns.  

	`Find a record with city='Philadelphia' AND job='software nerd'`
	
* OR is used to span multiple values in the same column.  

	`Find a record with (city='Philadelphia' OR city='Washington DC') AND job='software nerd'`

* IN is functionally equivalent to OR in this case.

As such, I've decided to implement AND & IN, but not OR.  Like I said, this covers 99% of what I do.  I'll gladly look at any reasonable patches to add OR functionality.

### AND

Mutliple keys are expanded with AND.  I have found AND to be much more useful than OR when querying on multiple columns.

	(where-clause {:id 1 :name "Sean"}) => "id=1 AND name=\"Sean\""

### IN
	
Suppose we want to know everyone named "Sean" or "Bill".  This can be done simply by placing and array in the conditions map

	(where-clause {:name ["Sean" "Bill"]}) => "name IN (\"Sean\", \"Bill\")"	

#TO DO

* where-clause needs to "pass through" a string 