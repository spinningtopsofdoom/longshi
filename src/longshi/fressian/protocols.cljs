(ns longshi.fressian.protocols)

(defprotocol FressianWriter
  (write-null! [fw])
  (write-boolean! [fw b])
  (write-int! [fw i])
  (write-long! [fw l])
  (write-float! [fw d])
  (write-double! [fw d])
  (write-string! [fw s])
  (write-bytes! [fw b])
  (write-object! [fw o] [fw o cache])
  (write-as! [fw tag o] [fw tag o cache]))

(defprotocol FressianReader
  (read-boolean! [fr])
  (read-float! [fr])
  (read-double! [fr])
  (read-int! [fr])
  (read-object! [fr]))
