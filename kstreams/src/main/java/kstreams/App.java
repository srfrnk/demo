package kstreams;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Connectors examples/docs:
 * https://www.confluent.io/blog/ksql-in-action-enriching-csv-events-with-data-from-rdbms-into-AWS/
 * https://kafka-connect-fs.readthedocs.io/_/downloads/en/latest/pdf/
 *
 * Dedup examples/docs:
 * https://blog.softwaremill.com/de-de-de-de-duplicating-events-with-kafka-streams-ed10cfc59fbe
 */
public class App {
  private static Logger logger = LoggerFactory.getLogger(App.class);
  private static final String KAFKA_BROKER =
      "my-cluster-kafka-0.my-cluster-kafka-brokers.kafka.svc:9092";

  public static void main(String[] args) {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-kafka2kafka");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

    final StreamsBuilder builder = new StreamsBuilder();

    builder.<String, String>stream("demo").map((key, value) -> {
      String[] cols = value.split(",");
      return new KeyValue<>(cols[0], cols[1]);
    }).map((key, value) -> new KeyValue<>(key, SomeClient.callAPI(value)))
        .map((key, value) -> new KeyValue<>(null, String.format("%s,%s", key, value)))
        .to("demo_enriched_streams");

    final Topology topology = builder.build();
    final KafkaStreams streams = new KafkaStreams(topology, props);
    final CountDownLatch latch = new CountDownLatch(1);

    // attach shutdown handler to catch control-c
    Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
      @Override
      public void run() {
        streams.close();
        latch.countDown();
      }
    });

    try {
      streams.start();
      latch.await();
    } catch (Throwable e) {
      System.exit(1);
    }
    System.exit(0);
  }
}
