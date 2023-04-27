package kafka;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.coders.VarLongCoder;
import org.apache.beam.sdk.state.StateSpec;
import org.apache.beam.sdk.state.StateSpecs;
import org.apache.beam.sdk.state.ValueState;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.splittabledofn.RestrictionTracker;
import org.apache.beam.sdk.values.KV;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.ReadableDuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Reader<K, V> extends DoFn<byte[], KV<K, V>> {
  private static Logger logger = LoggerFactory.getLogger(Reader.class);

  // @StateId("recordsConsumed")
  // private final StateSpec<ValueState<Long>> recordsConsumedSpec =
  //     StateSpecs.value(VarLongCoder.of());

  private Options<K, V> options;

  transient private int id = (int) (Math.random() * Integer.MAX_VALUE);
  transient KafkaConsumer<K, V> consumer;

  Reader(Options<K, V> options) {
    this.options = options;
  }

  @Setup
  public void setup() {
    consumer = Helper.getConsumer(options);
  }

  @StartBundle
  public void startBundle(StartBundleContext context) {}

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
  public ProcessContinuation processElement(@Element byte[] dummy,
      RestrictionTracker<ReaderRestriction, ReaderPosition> tracker,
      OutputReceiver<KV<K, V>> output/* , */, BundleFinalizer bundleFinalizer
  /* @StateId("recordsConsumed") ValueState<Long> recordsConsumedState */) throws Exception {
    ReaderRestriction restriction = tracker.currentRestriction();
    Long recordsConsumed = restriction.totalConsumed;//recordsConsumedState.read();
    // logger.debug("recordsConsumed: {}", recordsConsumed);

    bundleFinalizer.afterBundleCommit(Instant.now().plus(Duration.standardMinutes(10)), () -> {
      logger.debug("afterBundleCommit");
    });

    if (tracker.tryClaim(ReaderPosition.of(id))) {
      consumer.subscribe(Arrays.asList(options.topic()));
      ConsumerRecords<K, V> records = consumer.poll(java.time.Duration.ofMillis(100));
      for (ConsumerRecord<K, V> record : records) {
        K key = record.key();
        V value = record.value();
        // ReaderPosition position = ReaderPosition.of(recordsConsumed, record.partition());
        recordsConsumed++;
        output.output(KV.of(key, value));
      }
      // recordsConsumedState.write(recordsConsumed);
      return ProcessContinuation.resume();
    } else {
      return ProcessContinuation.stop();
    }
  }

  @GetInitialRestriction
  public ReaderRestriction getInitialRestriction() {
    return new ReaderRestriction(10000000, consumer.partitionsFor(options.topic()).size());
  }

  @GetRestrictionCoder
  public Coder<ReaderRestriction> getRestrictionCoder() {
    return SerializableCoder.of(ReaderRestriction.class);
  }
}
