(ns longshi.fressian.codes
  "Maps of fressian byte codes and of tag to byte codes")

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
       :STRUCTTYPE 0xEF
       :STRUCT 0xF0
       :STRUCT_CACHE_PACKED_START 0xA0
       :RESET_CACHES 0xFE
       :PRIORITY_CACHE_PACKED_START 0x80
       :GET_PRIORITY_CACHE 0xCC
       :PUT_PRIORITY_CACHE 0xCD
       :FOOTER_MAGIC 0xCFCFCFCF
       :FOOTER 0xCF
       :BEGIN_CLOSED_LIST 0xED
       :BEGIN_OPEN_LIST 0xEE
       :END_COLLECTION 0xFD
       :LIST_PACKED_LENGTH_START 0xE4
       :LIST_PACKED_LENGTH_END 0xEC
       :LIST 0xEC
       :MAP 0xC0
       :SET 0xC1
       :UUID 0xC3
       :REGEX 0xC4
       :URI 0xC5;
       :BIGINT 0xC6
       :BIGDEC 0xC7
       :INST 0xC8
       :SYM 0xC9
       :KEY 0xCA
       })

(def tag-to-code
  #js {
       "map" (.-MAP codes)
       "set" (.-SET codes)
       "uuid" (.-UUID codes)
       "regex" (.-REGEX codes)
       "uri" (.-URI codes)
       "bigint" (.-BIGINT codes)
       "bigdec" (.-BIGDEC codes)
       "inst" (.-INST codes)
       "sym" (.-SYM codes)
       "key" (.-KEY codes)
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
       :STRUCT_CACHE_PACKED_END 16
       :PRIORITY_CACHE_PACKED_END 32
       })
