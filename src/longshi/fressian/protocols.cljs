(ns longshi.fressian.protocols)

(defprotocol FressianWriter
  (write-null! [fw])
  (write-boolean! [fw b])
  (write-int! [fw i])
  (write-long! [fw l])
  (write-double! [fw d])
  (write-string! [fw s])
  (write-bytes! [fw b]))

(defprotocol FressianReader
  (read-double! [fr])
  (read-object! [fr]))
