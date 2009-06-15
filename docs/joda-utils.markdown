#Date Utilities

Sean Devlin 

June 11, 2009

Namespace: lib.devlinsf.joda-utils

This library is designed to wrap the Joda Time library from clojure.  It was inspired by Rich Hickey's work with sequence abstractions.

It depends on `clojure.contrib.str-utils`, `lib.devlinsf.date-utils`, and the Joda-time 1.6 jar.

#Extending to-ms-count

The first thing this library does is add cases the to-ms-count method for the following classes:

	defmethod to-ms-count org.joda.time.DateTime
	defmethod to-ms-count org.joda.time.Instant
	defmethod to-ms-count org.joda.time.base.BaseDateTime

This way there is now a way to convert Joda time data types to standard java data types.  Next the following constructor functions
are defining

	defn datetime 	=> returns org.joda.time.DateTime
	defn instant	=> returns org.joda.time.Instant
	
These functions are written in terms of to-ms-count, so that they have the broad range of inputs you'd expect.

#Creating a duration
The next function that is created is a duration constructor.  The Joda time library provides two styles of constructors.  The first take a long number of ms.  The
second implementation takes a start and stop time.

	(defn duration
		"Creates a Joda-Time Duration object"
		([duration] (org.joda.time.Duration. (long duration)))
		([start stop] (org.joda.time.Duration. (to-ms-count start) (to-ms-count stop))))
		
Notice that the second method has two calls to the `to-ms-count` function.  This makes is possible to create a duration object using the following inputs:

* java.lang.Long
* java.util.Date
* java.util.Calenedar
* java.sql.Timestamp
* clojure.lang.map
* org.joda.time.DateTime
* org.joda.time.Instant
* org.joda.time.base.BaseDateTime

The `to-ms-count` multimethod now begins to behave like a Java interface, allowing a broad range of inputs.

#Adding/Subtracting Durations

	defn add-dur
	[input-time duration]
	[input-time duration & durations]

	defn sub-dur
	[input-time duration]
	[input-time duration & durations]

These methods add/subtract a duration from any time abstraction that interacts with the `to-ms-count` function, and returns a new DateTime object.
This is the beginnings of a universal time manipulation library.  More to come.
