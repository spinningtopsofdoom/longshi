(ns longshi.fressian.protocols)

(defprotocol SeekStream
  (seek! [ss pos]))

(defprotocol WriteStream
  (write! [ws b])
  (writeBytes! [ws b off len])
  (getBytes [ws]))

(defprotocol ReadStream
  (read! [rs])
  (readBytes! [rs b off len])
  (available [rs]))

(defprotocol FressianWriter
  (writeNull! [fw])
  (writeBoolean! [fw b])
  (writeInt! [fw i])
  (writeDouble! [fw d]))

(defprotocol FressianReader
  (readDouble! [fr])
  (readObject! [fr]))