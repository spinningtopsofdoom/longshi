#Longshi
A ClojureScript implementation of [fressian](https://github.com/Datomic/fressian)

##Goals
* Performance - Encoding and decoding should be as fast as possible
* Not an island - The implementation should be available for JavaScript, ClojureScript and be able to run on modern JavaScript platform (i.e. browsers, nodejs, etc.)
* Minimal dependencies - The only dependencies are ClojureScript and the Google Closure library
* Minimal Size - For the browser implementation the file size should be as small as possible

##Status
Snapshot release - Still getting all fressian features implemented.  The api will closely mirror [data.fressian](https://github.com/clojure/data.fressian)

##Differnces from the canonical (java) implementation
* Numbers - Due to JavaScript numbers being doubles the maximum integer returned on the decoding side is -2^52 to (2^52 - 1) and anything over that is a Google Closure [long](http://docs.closure-library.googlecode.com/git/class_goog_math_Long.html) object
* Caching -  Since JavaScript doesn't have a default hashing implementation fo it's objects (ala Object.hashCode in java) in addition to a type's handler having encoding and decoding functions an option hashing function can be supplied.  This is so that the caching implementation can be efficient
* Data Types - JavaScript objects will be in the default set of handlers.  The Map, Set, Vector, and List handlers will be ClojureScript data types.

##Fressian Undocumented Assumptions
* CRC Checksum using [Adler-32](http://en.wikipedia.org/wiki/Adler-32) (in [RawOutput](https://github.com/Datomic/fressian/blob/master/src/org/fressian/impl/RawOutput.java) and [RawInput](https://github.com/Datomic/fressian/blob/master/src/org/fressian/impl/RawInput.java))
* Endianness - The endianness of fressian is the endianness of Java which is big endian.

##Compatability
The JavaScript implementation needs to have [Typed Arrays](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Typed_arrays) and [DataView](https://developer.mozilla.org/en-US/docs/Web/API/DataView)
