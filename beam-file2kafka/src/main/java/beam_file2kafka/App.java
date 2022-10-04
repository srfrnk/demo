package beam_file2kafka;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  private static final String HEADER_INPUT = "name,query";
  private static final String KAFKA_BROKER = "my-cluster-kafka-0.my-cluster-kafka-brokers.kafka.svc:9092";
  private static Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
    Pipeline pipeline = Pipeline.create(options);
    PCollection<String> linesInput = pipeline.apply(new ReadFile(HEADER_INPUT));
    linesInput.apply(KafkaIO.<Void, String>write()
    .withBootstrapServers(KAFKA_BROKER)
    .withTopic("demo")
    .withValueSerializer(StringSerializer.class)
    .values());
    pipeline.run();
  }
}
