package kafka;

import java.lang.reflect.InvocationTargetException;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TopicReader<K, V> extends DoFn<String, KV<String, Integer>> {
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
  public void processElement(@Element String topic, OutputReceiver<KV<String, Integer>> output)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException {
    KafkaConsumer<K, V> consumer = Helper.getConsumer(options);
    consumer.partitionsFor(topic).stream().forEach(p -> output.output(KV.of(topic, p.partition())));
    consumer.close();
  }
}
