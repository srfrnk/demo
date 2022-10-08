package beam_kafka2file;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;

public class App {
        private static final String HEADER_OUTPUT = "name,results";
        private static final String KAFKA_BROKER =
                        "my-cluster-kafka-0.my-cluster-kafka-brokers.kafka.svc:9092";
        private static Logger logger = LoggerFactory.getLogger(App.class);

        public static void main(String[] args) {
                PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
                Pipeline pipeline = Pipeline.create(options);
                PCollection<KV<Long, String>> linesInput = pipeline.apply(KafkaIO
                                .<Long, String>read().withMaxNumRecords(10000)
                                .withBootstrapServers(KAFKA_BROKER).withTopic("demo_enriched")
                                .withConsumerConfigUpdates(
                                                new ImmutableMap.Builder<String, Object>()
                                                                .put(ConsumerConfig.GROUP_ID_CONFIG,
                                                                                "kafka2file")
                                                                .put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                                                                                "earliest")
                                                                .build())
                                .withKeyDeserializer(LongDeserializer.class)
                                .withValueDeserializer(StringDeserializer.class).withReadCommitted()
                                .withoutMetadata());
                linesInput.apply("Remove keys",
                                MapElements.into(TypeDescriptors.strings())
                                                .via(KV<Long, String>::getValue))
                                .apply(new WriteFile(HEADER_OUTPUT));
                pipeline.run();
        }
}
