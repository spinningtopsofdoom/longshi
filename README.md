# Longshi

A ClojureScript implementation of [fressian](https://github.com/Datomic/fressian).
## Release information

Latest release: 0.1.3

[Leiningen](https://github.com/technomancy/leiningen) dependency information:

```clj
[longshi "0.1.3"]
```

## Examples
Currently the examples for the public api are [here](https://github.com/spinningtopsofdoom/longshi/blob/master/examples/core.cljs). More documentation will be forthcoming.

## Goals
* Performance - Encoding and decoding should be as fast as possible.
* Not an island - The implementation should be available for JavaScript, ClojureScript and be able to run on modern JavaScript platforms (i.e. browsers, nodejs, etc.)
* Minimal dependencies - The only dependencies are ClojureScript and the Google Closure library.
* Minimal Size - For the browser implementation, the file size should be as small as possible.

## Status
Alpha release - All fressian features have been ported over.  Encoding and decoding are currently significantly (around 40%) faster then the same operations on edn data.  The API will closely mirror [data.fressian](https://github.com/clojure/data.fressian).

## Differnces from the canonical (java) implementation
* Numbers - Due to JavaScript numbers being doubles, the maximum integer returned on the decoding side is -2^52 to (2^52 - 1) and anything over that is a Google Closure [long](http://docs.closure-library.googlecode.com/git/class_goog_math_Long.html) object.
* Caching - Since JavaScript doesn't have a default hashing implementation for its objects, (ala Object.hashCode in Java) and in addition to a type's handler having encoding and decoding functions, an optional hashing function can be supplied.  This is so that the caching implementation can be efficient.
* Data Types - JavaScript objects will be in the default set of handlers.  The Map, Set, Vector, and List handlers will be ClojureScript data types.
* Type Hierarchy - JavaScript does not have ability to construct type hierarchies like in Java.  This means handlers will need to be defined for every type to be written or read.
* Arrays - JavaScript does not have native boolean and long arrays so when those arrays are read in they will be read in as object arrays.  To write arrays of those types a custom type (TypedArray) is given that will allow those arrays to be written.
* Lists - For any Java list a JavaScript object array is used instead.

## Fressian Undocumented Assumptions
* CRC Checksum using [Adler-32](http://en.wikipedia.org/wiki/Adler-32) (in [RawOutput](https://github.com/Datomic/fressian/blob/master/src/org/fressian/impl/RawOutput.java) and [RawInput](https://github.com/Datomic/fressian/blob/master/src/org/fressian/impl/RawInput.java))
* Endianness - The endianness of fressian is the endianness of Java, which is big endian.

## Compatability
The JavaScript implementation needs to have [Typed Arrays](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Typed_arrays) and [DataView](https://developer.mozilla.org/en-US/docs/Web/API/DataView).
