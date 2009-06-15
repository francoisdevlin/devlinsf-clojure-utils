# My Utilities Library 
This is a collection of small utility routines I use in my code.  I put it up here in case anyone else finds it useful. Currently it includes:

* String Utilities
* Date Utilities
* Joda-Time Utilities
* Predicate Utilities
* Clipboard Utilities

# String Utilities 

Namespace: lib.devlinsf.str-utils

This is a proposed change for str-utils.  There are a few key changes, which can be summarized as follows.

* The re-* methods can now take a list of regexes, and each is applied recursively.
* Several utility methods have been added to simply common string manipulations.
* str-take & str-drop methods have been created, which simplify substring operations and splitting once on a regex.
* re-split is written in terms of re-partition.  The result is re-split is now lazy.

## Usage is documented in the docs/str-utils.markdown file.

#Date Utilities

Namespace: lib.devlinsf.date-utils

This library is designed to add a standard way to construct & wrap date objects in Clojure.  It is intended to be a very broad purpose adapter.

It depends on `clojure.contrib.str-utils`

## Usage is documented in the docs/date-utils.markdown file.

#Joda-Time Utilities

Namespace: lib.devlinsf.joda-utils

This library is designed to wrap the Joda Time library from clojure.  It was inspired by Rich Hickey's work with sequence abstractions.

It depends on `clojure.contrib.str-utils`, `lib.devlinsf.date-utils`, and the Joda-time 1.6 jar.

## Usage is documented in the docs/joda-utils.markdown file.

#Predicate Utilities

Namespace: lib.devlinsf.predicate-utils

This is a namespace to assist with predicate creation and composition in clojure.  The AND & OR macros are good for testing, but not filtering.
This library attempts to fill those gaps.

## Usage is documented in the docs/predicate-utils.markdown file.

# Clipboard Utilities 

Namespace: lib.devlinsf.clip-utils

This is designed to support ad-hoc data processing and spreadsheet wrangling.  Also, it could be useful for adding custom cut and paste to your own Swing applications.

Currently only moving text objects between applications is supported.  There a few known quirks with set-clip! in OS X.  Seems to work fine on XP.  Have not tested Vista or any variants of Linux.

## Usage is documented in the docs/clip-utils.markdown file.

#SQL Utilies

Namespace: lib.devlinsf.sql-utils

This is designed to wrap common sql utilities.  I current don't use much of it directly.  The macros in `lib.devlinsf.model-utilities` are preferred instead.  It is worth
knowing how to use `connection-map` and `where-clause` functions.

# Installation

After downloading, run ant in order to build the devlinsf-utils.jar file.  Then, add it to you clojure classpaths as necessary
