package kafka;

import java.util.Arrays;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.splittabledofn.RestrictionTracker;
import org.apache.beam.sdk.values.KV;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PartitionReader<K, V> extends DoFn<KV<String, Integer>, KV<K, V>> {
  private static Logger logger = LoggerFactory.getLogger(PartitionReader.class);

  private Options<K, V> options;

  transient KafkaConsumer<K, V> consumer;
  PartitionPosition bundleWaterline;

  PartitionReader(Options<K, V> options) {
    this.options = options;
  }

  @Setup
  public void setup() {
    consumer = Helper.getConsumer(options);
  }

  @StartBundle
  public void startBundle(StartBundleContext context) {
    bundleWaterline = PartitionPosition.of(0);
  }

  @FinishBundle
  public void finishBundle(FinishBundleContext context) {}

  @Teardown
  public void teardown() {
    if (consumer != null) {
      consumer.unsubscribe();
      consumer.close();
      consumer = null;
    }
  }

  @ProcessElement
  public ProcessContinuation processElement(@Element KV<String, Integer> topicPartition,
      RestrictionTracker<PartitionRestriction, PartitionPosition> tracker,
      OutputReceiver<KV<K, V>> output) throws Exception {

    consumer.subscribe(Arrays.asList(topicPartition.getKey()));

    // Deserializer<K> keyDeserializer = keyDeserializerClass.getDeclaredConstructor().newInstance();
    // Deserializer<V> valueDeserializer =valueDeserializerClass.getDeclaredConstructor().newInstance();

    PartitionPosition pollWaterline = bundleWaterline;
    ConsumerRecords<K, V> records = consumer.poll(java.time.Duration.ofMillis(1000));
    for (ConsumerRecord<K, V> record : records) {
      K key = record.key();
      V value = record.value();
      pollWaterline = pollWaterline.advance(record.offset());
      if (tracker.tryClaim(bundleWaterline)) {
        output.output(KV.of(key, value));
      } else {
        return ProcessContinuation.stop();
      }
    }
    bundleWaterline = pollWaterline;
    return ProcessContinuation.resume();
  }

  @GetInitialRestriction
  public PartitionRestriction getInitialRestriction(@Element KV<String, Integer> topicPartition) {
    return new PartitionRestriction(new PartitionPosition(-1));
  }

  @GetRestrictionCoder
  public Coder<PartitionRestriction> getRestrictionCoder() {
    return SerializableCoder.of(PartitionRestriction.class);
  }
}
