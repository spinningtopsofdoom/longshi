(ns longshi.fressian.protocols
  "Protocols for reading, writing and streaming fressian data")

(defprotocol FressianWriter
  "Protocol for writing values to a fressian bytestream

  All methods append to the bytestream.  All writers return the bytestream"
  (write-null! [fw]
    "Writes a null value to the fressian bytestream")
  (write-boolean! [fw b]
    "Writes a boolean value to the fressian bytestream

     The value passed in is converted to true or false values
     based on the truthiness of the value in ClojureScript")
  (write-int! [fw i]
    "Writes an integer value to the fressian bytestream

     Any non integer values are truncated (i.e. 2.3 => 2 and 555.77 => 555)")
  (write-long! [fw l]
    "Writes a Google Closure Long object to the fressian bytestream

     The Google Closure Long will be written as a primitive long.")
  (write-float! [fw f]
    "Writes a 32 bit floating point number to the fressian bytestream

     Regular JavaScript numbers can be passed in and will convereted to floats.
     Note that since JavaScript numbers are doubles precision will be lost
     (i.e. 0.1 => 0.10000000149011612)")
  (write-double! [fw d]
    "Writes a 64 bit floating point number to the fressian bytestream

     Since JavaScript numbers are doubles this will write out a number without any conversions")
  (write-string! [fw s]
    "Writes a utf-8 string to the fressian bytestream

     All strings passed to this method are expected to be utf-8 encoded")
  (write-bytes! [fw b]
    "Writes an array of bytes to the fressian bytestream

     Only the typed arrays Uint8Array and Int8Array should be passed in.")
  (write-list! [fw l]
    "Writes a sequential value to the fressian bytestream

     This works for JavaScript arrays and any ClojureScript value that can
     be made into a sequence")
  (write-tag! [fw tag component-count]
    "Writes out a tag and the number of components to the fressian bytestream

     tag (String) - The fressian name of the type (does not have to be actual type name).
                    After the first write using this tag the tag is cached as a single byte.
     component-count (number) - The number of values that will be written after this method
                                that are part of the type

     This is for writing out custom / extended types.  Here is an example

     (deftype Point [x y])
     ;;Writes marker for the Point type
     (write-tag! fw \"my-point\" 2)
     ;;Point's components
     (write-int! -1)
     (write-int! 1)
     ;;Writes out Point in a different way
     (write-tag! fw \"another-point\" 2)
     (write-double! -0.5)
     (write-double! 0.5)
     ;;Writing a ClojureScript vector
     (def samples [56 44 78 93 58 66])
     (write-tag! fw \"vector\" 1)
     (write-list! fw samples)
     ")
  (write-object! [fw o] [fw o cache]
    "Writes out a value to the fressian bytestream

     o (Object) - The value to be written
     cache (boolean) - If the value is to be cached.  Defaults to false.

     How the value is written out depends on it's type.  JavaScript numbers are
     written as integers or doubles.")
  (write-as! [fw tag o] [fw tag o cache]
    "Writes out a value to the fressian bytestream using the tag handler.

     o (Object) - The value to be written
     tag (String|nil) - The name of the handler to use.  If nil or \"any\" is used
                        the handler is determined by the values type.
     cache (boolean) - If the value is to be cached.  Defaults to false.")
  (reset-caches! [fw]
    "Resets the writers caches and writes a reset cache message to the fressian bytestream

     The caches that are reset are the type name caching and object caching")
  (write-footer! [fw]
    "Writes out footer and checksum to the fressian bytestream

     The checksum is calculated over the bytestream using Adler32.  All caches are cleared
     when the checksum is written"))

(defprotocol StreamingWriter
  "Protocol for streaming fressian data"
  (begin-closed-list! [sw]
    "Writes marker to begin a closed fressian list

     To end the list use the end-list! method.  This list is used for writing sequential
     data in advance.")
  (end-list! [sw]
    "Writes marker for ending a fressian list.")
  (begin-open-list! [sw]
    "Writes marker to begin a open fressian list

     This must be called at the begining of the stream.  To end the list use the end-list!
     method or close the stream.  This list is used for writing sequential data where the
     size is not known and the stream failing can be interpreted as the end of the list.")
  (write-footer-for! [sw bb]
    "Appends bytestream and footer to the current fressian bytestream

    This method can only be called at the begining or after a footer is written"))

(defprotocol FressianReader
  "Protocols for reading a fressian bytestream"
  (read-boolean! [fr]
    "Gets a boolean from the bytestream")
  (read-float! [fr]
    "Gets a 32 bit floating point number from the bytestream")
  (read-double! [fr]
    "Gets a 64 bit floating point number from the bytestream")
  (read-int! [fr]
    "Gets an integer from the bytestream

     Due to JavaScript numbers being doubles numbers not range -2 ^ 51 to (2 ^ 51) - 1
     are returned as Google Closure Longs")
  (read-object! [fr]
    "Reads a value from the bytestream")
  (validate-footer! [fr]
    "Reads the footer from the bytestream and performs validation

     The validations performed are checking the footer marker, the bytestream length, and optionally
     the checksum"))

(defprotocol Cached
  "Fressian Caching protocol"
  (cached-value [co]
    "Gets the value from the cache"))
