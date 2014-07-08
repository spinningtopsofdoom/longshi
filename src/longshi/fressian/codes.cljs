(ns longshi.fressian.codes)

(def codes
  #js {
       :NULL 0xF7
       :TRUE 0xF5
       :FALSE 0xF6
       :FLOAT 0xF9
       :DOUBLE 0xFA
       :DOUBLE_0 0xFB
       :DOUBLE_1 0xFC
       :INT_PACKED_2_ZERO 0x50
       :INT_PACKED_3_ZERO 0x68
       :INT_PACKED_4_ZERO 0x72
       :INT_PACKED_5_ZERO 0x76
       :INT_PACKED_6_ZERO 0x7A
       :INT_PACKED_7_ZERO 0x7E
       :INT 0xF8
       :STRING 0xE3
       :STRING_PACKED_LENGTH_START 0xDA
       :STRING_CHUNK 0xE2
       :BYTES_CHUNK 0xD8
       :BYTES 0xD9
       :BYTES_PACKED_LENGTH_START 0xD0
       :LONG_ARRAY 0xB0
       :DOUBLE_ARRAY 0xB1
       :BOOLEAN_ARRAY 0xB2
       :INT_ARRAY 0xB3
       :FLOAT_ARRAY 0xB4
       :OBJECT_ARRAY 0xB5
       })

(def tag-to-code
  #js {
       "int[]" (.-INT_ARRAY codes)
       "float[]" (.-FLOAT_ARRAY codes)
       "double[]" (.-DOUBLE_ARRAY codes)
       "long[]" (.-LONG_ARRAY codes)
       "boolean[]" (.-BOOLEAN_ARRAY codes)
       "Object[]" (.-OBJECT_ARRAY codes)
       })

(def ranges
  #js {
       :STRING_PACKED_LENGTH_END 8
       :BYTES_PACKED_LENGTH_END 8
       :BYTE_CHUNK_SIZE 65535
       })
