(ns longshi.fressian.byte-stream-protocols)

(defprotocol SeekStream
  (seek! [ss pos]))

(defprotocol WriteStream
  (write! [ws b])
  (write-bytes! [ws b off len])
  (get-bytes [ws]))

(defprotocol IntegerWriteStream
  (write-int16! [iws i16])
  (write-int24! [iws i24])
  (write-int32! [iws i32])
  (write-unsigned-int16! [iws ui16])
  (write-unsigned-int24! [iws ui24])
  (write-unsigned-int32! [iws ui32]))

(defprotocol FloatWriteStream
  (write-float! [fws f]))

(defprotocol DoubleWriteStream
  (write-double! [dws d]))

(defprotocol ReadStream
  (read! [rs])
  (read-bytes! [rs b off len])
  (available [rs]))

(defprotocol IntegerReadStream
  (read-int16! [irs])
  (read-int24! [irs])
  (read-int32! [irs])
  (read-unsigned-int16! [irs])
  (read-unsigned-int24! [irs])
  (read-unsigned-int32! [irs]))

(defprotocol FloatReadStream
  (read-float! [frs]))

(defprotocol DoubleReadStream
  (read-double! [drs]))
