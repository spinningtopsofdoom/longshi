(ns longshi.fressian.byte-stream-protocols)

(defprotocol SeekStream
  (seek! [ss pos]))

(defprotocol WriteStream
  (write! [ws b])
  (write-bytes! [ws b off len])
  (get-bytes [ws]))

(defprotocol ReadStream
  (read! [rs])
  (read-bytes! [rs b off len])
  (available [rs]))
