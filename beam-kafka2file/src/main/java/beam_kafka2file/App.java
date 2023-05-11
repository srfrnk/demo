package beam_kafka2file;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.beam.runners.direct.DirectOptions;
import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kafka.Options;
import kafka.Read;

public class App {
  private static final String HEADER_OUTPUT = "name,results";
  private static final String KAFKA_BROKER =
      "my-cluster-kafka-0.my-cluster-kafka-brokers.kafka.svc:9092";
  private static Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    args = Arrays.asList(args).stream().map(s -> s.trim()).collect(Collectors.toList())
        .toArray(new String[0]);
    PipelineOptionsFactory.register(AppPipelineOptions.class);

    AppPipelineOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(AppPipelineOptions.class);
    if (options.getRunner().equals(DirectRunner.class)) {
      DirectOptions directOptions = (DirectOptions) options.as(DirectOptions.class);
      directOptions.setBlockOnRun(false);
    }

    Pipeline pipeline = Pipeline.create(options);

    PCollection<KV<Long, String>> linesInput = pipeline.apply(Read.<Long, String>withOptions(
        Options.<Long, String>builder().withTopic("demo").withGroupId("kafka2file")
            // .withMaxNumRecords(10000)
            .withIsolationLevel(IsolationLevel.READ_COMMITTED).withBootstrapServers(KAFKA_BROKER)
            .withAutoOffsetResetConfig(OffsetResetStrategy.EARLIEST)
            .withKeyDeserializer(LongDeserializer.class)
            .withValueDeserializer(StringDeserializer.class).build()));

    linesInput.apply(MapElements.into(TypeDescriptors.voids()).via((KV<Long, String> kv) -> {
      logger.info("ELEMENT: {} {}", kv.getKey(), kv.getValue());
      return null;
    }));
    linesInput
        .apply("Remove keys",
            MapElements.into(TypeDescriptors.strings()).via(KV<Long, String>::getValue))
        .apply(
            TextIO.write().to(options.getOutputFile()).withHeader(HEADER_OUTPUT).withoutSharding());

    PipelineResult results = pipeline.run();
    logger.info("Running Pipeline!");
    results.waitUntilFinish();
    logger.info("Exiting Pipeline!");
  }
}

