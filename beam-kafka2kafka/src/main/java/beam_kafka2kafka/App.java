package beam_kafka2kafka;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;

public class App {
        private static final String KAFKA_BROKER =
                        "my-cluster-kafka-0.my-cluster-kafka-brokers.kafka.svc:9092";
        private static Logger logger = LoggerFactory.getLogger(App.class);

        public static void main(String[] args) {
                PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
                Pipeline pipeline = Pipeline.create(options);
                PCollection<KV<String, String>> linesInput = pipeline.apply(KafkaIO
                                .<Long, String>read().withMaxNumRecords(10000)
                                .withBootstrapServers(KAFKA_BROKER).withTopic("demo")
                                .withConsumerConfigUpdates(
                                                new ImmutableMap.Builder<String, Object>()
                                                                .put(ConsumerConfig.GROUP_ID_CONFIG,
                                                                                "kafka2kafka")
                                                                .put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                                                                                "earliest")
                                                                .build())
                                .withKeyDeserializer(LongDeserializer.class)
                                .withValueDeserializer(StringDeserializer.class).withReadCommitted()
                                .withoutMetadata())
                                .apply("Remove keys",
                                                MapElements.into(TypeDescriptors.strings())
                                                                .via(KV<Long, String>::getValue))
                                .apply("Split columns",
                                                MapElements.into(TypeDescriptor.of(String[].class))
                                                                .via((String line) -> line
                                                                                .split(",")))
                                .setCoder(AvroCoder.of(TypeDescriptor.of(String[].class)))
                                .apply("Split to KV", MapElements
                                                .into(TypeDescriptors.kvs(TypeDescriptors.strings(),
                                                                TypeDescriptors.strings()))
                                                .via((String[] cols) -> KV.of(cols[0], cols[1])));

                PCollection<KV<String, String>> linesOutput = linesInput.apply("Enrich data",
                                ParDo.of(new DoFn<KV<String, String>, KV<String, String>>() {
                                        @ProcessElement
                                        public void processElement(ProcessContext c) {
                                                KV<String, String> kv = c.element();
                                                String name = kv.getKey();
                                                String query = kv.getValue();

                                                // logger.info("** {} Start: {}", name, query);
                                                String results = SomeClient.callAPI(query);
                                                // logger.info("** {} End: {}", name, results);

                                                c.output(KV.of(name, results));
                                        }
                                }));

                linesOutput.apply("Merge columns", MapElements.into(TypeDescriptors.strings())
                                .via((KV<String, String> cols) -> String.format("%s,%s",
                                                cols.getKey(), cols.getValue())))
                                .apply(KafkaIO.<Void, String>write()
                                                .withBootstrapServers(KAFKA_BROKER)
                                                .withTopic("demo_enriched")
                                                .withValueSerializer(StringSerializer.class)
                                                .values());
                pipeline.run();
        }
}
