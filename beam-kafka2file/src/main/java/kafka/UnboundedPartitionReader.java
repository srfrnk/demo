package kafka;

import java.util.Arrays;
import java.util.HashMap;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.splittabledofn.RestrictionTracker;
import org.apache.beam.sdk.values.KV;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UnboundedPartitionReader<K, V> extends DoFn<KV<String, Integer>, KV<K, V>> {
  private static Logger logger = LoggerFactory.getLogger(UnboundedPartitionReader.class);

  private Options<K, V> options;

  transient KafkaConsumer<K, V> consumer;
  transient HashMap<TopicPartition, OffsetAndMetadata> maxConsumedOffsets;

  UnboundedPartitionReader(Options<K, V> options) {
    this.options = options;
  }

  @Setup
  public void setup() {
    consumer = Helper.getConsumer(options);
  }

  @StartBundle
  public void startBundle(StartBundleContext context) {
    maxConsumedOffsets = new HashMap<>();
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
      OutputReceiver<KV<K, V>> output, BundleFinalizer bundleFinalizer) throws Exception {
    if (tracker.tryClaim(new PartitionPosition())) {
      bundleFinalizer.afterBundleCommit(Instant.now().plus(Duration.standardSeconds(10)), () -> {
        commitMaxConsumedOffsets();
      });

      consumer.subscribe(Arrays.asList(topicPartition.getKey()));
      ConsumerRecords<K, V> records =
          consumer.poll(java.time.Duration.ofMillis(options.pollTimeMS()));
      for (ConsumerRecord<K, V> record : records) {
        K key = record.key();
        V value = record.value();
        output.output(KV.of(key, value));
        updateMaxConsumedOffsets(record);
      }
      return ProcessContinuation.resume();
    } else {
      return ProcessContinuation.stop();
    }
  }

  public void commitMaxConsumedOffsets() {
    consumer.commitSync(maxConsumedOffsets);
    maxConsumedOffsets = null;
  }

  public void updateMaxConsumedOffsets(ConsumerRecord<K, V> record) {
    String topic = record.topic();
    int partition = record.partition();
    long offset = record.offset() + 1;
    // Need to commit offset for next message to poll: https://kafka.apache.org/34/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html#commitSync(java.util.Map)

    TopicPartition tp = new TopicPartition(topic, partition);
    var maxConsumedOffset = maxConsumedOffsets.getOrDefault(tp, new OffsetAndMetadata(0));
    maxConsumedOffset = new OffsetAndMetadata(Math.max(offset, maxConsumedOffset.offset()));
    maxConsumedOffsets.put(tp, maxConsumedOffset);
  }

  @GetInitialRestriction
  public PartitionRestriction getInitialRestriction(@Element KV<String, Integer> topicPartition) {
    return new PartitionRestriction();
  }

  @GetRestrictionCoder
  public Coder<PartitionRestriction> getRestrictionCoder() {
    return SerializableCoder.of(PartitionRestriction.class);
  }
}
