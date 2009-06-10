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

#`where-clause [conditions-map]`

The goal was to have the following s-expression generate valid SQL

(str "select * from FOO where " (where-clause _conditions-map_) )

This function takes a map as inputs, and expands it to a SQL where clause.  This is probably best explained by example.


## Substitution Examples

First, consider the most basic condition you could think of.

    (where-clause {:id 1}) => "id=1"

Substituting into our SQL builder above yeilds

    "select * from FOO where id=1"

The map can also have string keys

    (where-clause {"id" 1}) => "id=1"

Mutliple keys are expanded with AND.  I have found AND to be much more useful than OR when querying on multiple columns.

	(where-clause {:id 1 :name "Sean"}) => "id=1 AND name=\"Sean\""
	
However, suppose we want to know everyone named "Sean" or "Bill".  This can be done simply by placing and array in the conditions map

	(where-clause {:name ["Sean" "Bill"}) => "name IN (\"Sean\", \"Bill\")"
	
Notice how the array is expanded to an IN statement.  This is a more concise way of using OR for multiple values in the same column (which is my main use for OR).

#TO DO

* where-clause needs to "pass through" a string
* (where-clause {:id nil}) should generate "ID IS NULL"
 