#Date Utilities

Sean Devlin 

June 11, 2009

Namespace: lib.devlinsf.date-utils

This library is designed to add a standard way to construct & wrap date objects in Clojure.  It is intended to be a very broad purpose adapter.

It depends on `clojure.contrib.str-utils`

#date[& params]

This function is designed to create a java.util.Date object.

	;This code was executed on June 11, 2009, near 7PM
	(date)
	=> <Date Thu Jun 11 19:07:28 EDT 2009>
	
The date method can also take a list of keywords followed by values

	user=> (date :year 1982)
	#<Date Fri Jun 11 19:10:06 EDT 1982>

Notice that the "unset" values default to the current time and date.
	
	user=> (date :year 1982 :month 2 :day 4)
	#<Date Thu Mar 04 19:11:35 EST 1982>

Passing a map works just a easily:

	user=> (date {:year 1982 :month :2 :day 4})
	#<Date Thu Mar 04 19:31:30 EST 1982>

The full list of presently usable field keywords: 

	keyword			Java Constant
	===============================================
	:year 			java.util.Calendar/YEAR
    :month 			java.util.Calendar/MONTH
    :day 			java.util.Calendar/DAY_OF_MONTH
    :hour 			java.util.Calendar/HOUR
    :hour-of-day 	java.util.Calendar/HOUR_OF_DAY
    :minute 		java.util.Calendar/MINUTE
    :second 		java.util.Calendar/SECOND
    :ms 			java.util.Calendar/MILLISECOND
    :day-of-week 	java.util.Calendar/DAY_OF_WEEK

## Setting the Month
	
In the example above, notice that month 2 corresponds to March, not February.  This is done to match the Java api.  However, that's a PITA.
The month field can also take a keyword.
	
	user=> (date :year 1982 :month :march :day 4)
	#<Date Thu Mar 04 19:31:30 EST 1982>

Or, use the three letter shorthand:

	user=> (date :year 1982 :month :mar :day 4)
	#<Date Thu Mar 04 19:31:30 EST 1982>

Much better!  The complete list of month keywords:

	keyword			Java Constant
	========================================
	:jan 		java.util.Calendar/JANUARY
    :january 	java.util.Calendar/JANUARY
    :feb 		java.util.Calendar/FEBRUARY
    :february 	java.util.Calendar/FEBRUARY
    :mar 		java.util.Calendar/MARCH
    :march 		java.util.Calendar/MARCH
    :apr 		java.util.Calendar/APRIL
    :april 		java.util.Calendar/APRIL
    :may 		java.util.Calendar/MAY
    :jun 		java.util.Calendar/JUNE
    :june 		java.util.Calendar/JUNE
    :jul 		java.util.Calendar/JULY
    :july 		java.util.Calendar/JULY
    :aug 		java.util.Calendar/AUGUST
    :august 	java.util.Calendar/AUGUST
    :sep 		java.util.Calendar/SEPTEMBER
    :september 	java.util.Calendar/SEPTEMBER
    :oct 		java.util.Calendar/OCTOBER
    :october 	java.util.Calendar/OCTOBER
    :nov 		java.util.Calendar/NOVEMBER
    :november 	java.util.Calendar/NOVEMBER
    :dec 		java.util.Calendar/DECEMBER
    :december 	java.util.Calendar/DECEMBER

## Date Parsing

(Note: This parser currently only works for dates, not times.)

How about creating a date from a string?

	user=> (date "3/4/1982")
	#<Date Thu Mar 04 19:32:39 EST 1982>
	
	user=> (date "3-4-1982")
	#<Date Thu Mar 04 19:33:03 EST 1982>
	
Hmmm, this is good for Americans, but the rest of the world might prefer something else.  No problem.

	(date "4/3/1982" :order [:day :month :year])
	=> <Date Thu Mar 04 19:34:33 EST 1982>
	
Perfect! 

##nil behavior

This function returns nil if passed nil

	user=> (date nil)
	nil

##Wrapping other time formats

This function can also accept other time formats and return them as java.util.Date objects.  This method can take:

### java.lang.Long

	user=> (date (. (date :year 1970) getTime))
	#<Date Thu Jun 11 20:36:51 EDT 1970>

### java.util.Calendar

	user=> (date (java.util.GregorianCalendar. ))
	#<Date Thu Jun 11 20:38:00 EDT 2009>

### java.sql.Timestamp

	user=> (date 
			(java.sql.Timestamp. 
				(. (java.util.Date. ) getTime)))
	#<Date Thu Jun 11 20:38:48 EDT 2009>
	
### java.util.Date

	user=> (date (java.util.Date. ))
	#<Date Thu Jun 11 20:39:59 EDT 2009>

Each of the above functions "drills down" to the Long value, and builds a new Date object.  This way changing the original object won't affect the new date record.

#The Awesome part:

There are also four other methods in this library

	long-time 	returns java.lang.Long
	greg-cal 	returns java.util.GregorianCalendar
	sql-ts 		returns java.sql.Timestamp
	time-map 	returns clojure.lang.PersistentHashMap

Each of these has the exact same signature as `date`.  Every use of date shown above will work with these methods. For example, each of the following is valid:

	user=> (long-time)
	1244764606423

	user=> (greg-cal (date))
	#<GregorianCalendar java.util.GregorianCalendar[...]>
	
	user=> (sql-ts :year 1982 :month :march :day 4)
	#<Timestamp 1982-03-04 19:57:23.309>
	
	user=> (time-map "3/4/1982")
	{:minute 57, :hour-of-day 19, :day-of-week 5, :year 1982, :month 2, :day 4, :second 44, :ms 375}
	
Each of these methods also drills down to the Long, so that there is no object linking.  This also makes each of the methods a very versatile adapters.