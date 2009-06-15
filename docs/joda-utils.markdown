#Date Utilities

Sean Devlin 

June 11, 2009

Namespace: lib.devlinsf.joda-utils

This library is designed to wrap the Joda Time library from clojure.  

It depends on `clojure.contrib.str-utils`, `lib.devlinsf.date-utils`, and the Joda-time 1.6 jar.

#Extending to-ms-count

The first thing this library does is add cases the to-ms-count method for the following classes:

	defmethod to-ms-count org.joda.time.DateTime
	defmethod to-ms-count org.joda.time.BaseDateTime

This way there is now a way to convert Joda time data types to standard java data types.  Next the following constructor functions
are defining

	defn datetime 	=> returns org.joda.time.DateTime
	defn instant	=> returns org.joda.time.Instant
	
These functions are writing in terms of to-ms-count, so that the 