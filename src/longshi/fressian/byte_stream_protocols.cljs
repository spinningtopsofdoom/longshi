(ns longshi.fressian.byte-stream-protocols
  "Protocols for reading and writing bytestreams"
  (:refer-clojure :exclude [reset!]))

(defprotocol ByteBuffer
  "Protocol to get byte arrays out of a bytestream"
  (duplicate-bytes [bb]
    "Gets a byte array that is the copy of the bytestream")
  (get-bytes [bb]
    "Gets a byte array that is a reference to the bytestream"))

(defprotocol SeekStream
  "Protocol for seeling across a stream"
  (seek! [ss pos]
    "Moves stream pointer to pos

     pos (int) - Location to move bytesstream pointer"))

(defprotocol ResetStream
  "Protocol for reseting a bytestream"
  (reset! [rs]
    "Resets a fressian bytestream to begin writitng or reading"))

(defprotocol CheckedStream
  "Protocol for byte stream checksums"
  (get-checksum [cs]
    "Gets the checksum value for a bytestream"))

(defprotocol RawWriteStream
  "Protocol for a fressian writer bytestream"
  (bytes-written [rws]
    "Gets the number of bytes written to a fressian bytestream

     This is different then the total number of bytes in a bytestream.  A bytestream
     can contain many fressian bytestream."))

(defprotocol WriteStream
  "Protocol for writing bytestreams"
  (write! [ws b]
    "Writes a byte to the bytestream

     b (byte) - The byte to write to the stream")
  (write-bytes! [ws b off len]
    "Writes a byte array to the bytestream

     b ([byte]) - Byte array to write to the bytestream
     off (int) - The number of bytes to offset the writing by
     len (int) - The number of bytes in the byte array to write"))

(defprotocol IntegerWriteStream
  "Protocol for writing integers to a bytestream"
  (write-int16! [iws i16]
    "Writes a 16 bit integer to the bytestream")
  (write-int24! [iws i24]
    "Writes a 24 bit integer to the bytestream")
  (write-int32! [iws i32]
    "Writes a 32 bit integer to the bytestream")
  (write-unsigned-int16! [iws ui16]
    "Writes a 16 bit unsigned integer to the bytestream")
  (write-unsigned-int24! [iws ui24]
    "Writes a 24 bit unsigned integer to the bytestream")
  (write-unsigned-int32! [iws ui32]
    "Writes a 32 bit unsigned integer to the bytestream"))

(defprotocol FloatWriteStream
  "Protocol for writing floats to a bytestream"
  (write-float! [fws f]
    "Writes a 32 bit floating point to the bytestream"))

(defprotocol DoubleWriteStream
  "Protocol for writing doubles to a bytestream"
  (write-double! [dws d]
    "Writes a 64 bit floating point to the bytestream"))

(defprotocol RawReadStream
  "Protocol for a fressian reader bytestream"
  (bytes-read [rrs]
    "Gets the number of bytes read from a fressian bytestream

     This is different then the total number of bytes in a bytestream.  A bytestream
     can contain many fressian bytestream."))

(defprotocol ReadStream
  "Protocol for reading bytestreams"
  (read! [rs]
    "Reads a byte from the bytestream")
  (read-bytes! [rs b off len]
    "Writes a byte array to the bytestream

     b ([byte]) - Byte array that the bytestream will be put into
     off (int) - The number of bytes to offset the reading by
     len (int) - The number of bytes to read into the byte array")
  (available [rs]
    "How many bytes are unread in the bytesstream"))

(defprotocol IntegerReadStream
  "Protocol for reading integers from a bytestream"
  (read-int16! [irs]
    "Reads a 16 bit integer from the bytestream")
  (read-int24! [irs]
    "Reads a 24 bit integer from the bytestream")
  (read-int32! [irs]
    "Reads a 32 bit integer from the bytestream")
  (read-unsigned-int16! [irs]
    "Reads a 16 bit unsigned integer from the bytestream")
  (read-unsigned-int24! [irs]
    "Reads a 24 bit unsigned integer from the bytestream")
  (read-unsigned-int32! [irs]
    "Reads a 32 bit unsigned integer from the bytestream"))

(defprotocol FloatReadStream
  "Protocol for reading floats from a bytestream"
  (read-float! [frs]
    "Reads a 32 bit floating point from the bytestream"))

(defprotocol DoubleReadStream
  "Protocol for reading floats from a bytestream"
  (read-double! [drs]
    "Reads a 64 bit floating point from the bytestream"))
