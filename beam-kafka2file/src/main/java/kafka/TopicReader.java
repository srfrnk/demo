package kafka;

import java.lang.reflect.InvocationTargetException;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TopicReader<K, V> extends DoFn<byte[], KV<String, Integer>> {
  private static Logger logger = LoggerFactory.getLogger(TopicReader.class);

  private Options<K, V> options;

  TopicReader(Options<K, V> options) {
    this.options = options;
  }

  @Setup
  public void setup() {}

  @StartBundle
  public void startBundle(StartBundleContext context) {}

  @FinishBundle
  public void finishBundle(FinishBundleContext context) {}

  @Teardown
  public void teardown() {}

  @ProcessElement
  public void processElement(@Element byte[] dummy, OutputReceiver<KV<String, Integer>> output)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException {
    KafkaConsumer<K, V> consumer = Helper.getConsumer(options);
    consumer.partitionsFor(options.topic()).stream().forEach(partitionInfo -> {
      output.output(KV.of(options.topic(), partitionInfo.partition()));
    });
    consumer.close();
  }
}
