FROM quay.io/strimzi/kafka:0.31.1-kafka-3.2.3

USER root
COPY connect-file-sink.properties /opt/kafka/config/connect-file-sink.properties
COPY connect-file-source.properties /opt/kafka/config/connect-file-source.properties
COPY connect-standalone.properties /opt/kafka/config/connect-standalone.properties
COPY kafka-run-class.sh /opt/kafka/bin/kafka-run-class.sh

USER kafka
CMD [ "bin/connect-standalone.sh",\
  "config/connect-standalone.properties",\
  "config/connect-file-source.properties",\
  "config/connect-file-sink.properties" ]
