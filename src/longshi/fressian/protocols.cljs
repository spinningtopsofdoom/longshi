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
  (write-list! [fw l])
  (write-tag! [fw tag component-count])
  (write-object! [fw o] [fw o cache])
  (write-as! [fw tag o] [fw tag o cache])
  (reset-caches! [fw])
  (write-footer! [fw]))

(defprotocol StreamingWriter
  (begin-closed-list! [sw])
  (end-list! [sw])
  (begin-open-list! [sw])
  (write-footer-for! [sw bb]))

(defprotocol FressianReader
  (read-boolean! [fr])
  (read-float! [fr])
  (read-double! [fr])
  (read-int! [fr])
  (read-object! [fr])
  (validate-footer! [fr]))
