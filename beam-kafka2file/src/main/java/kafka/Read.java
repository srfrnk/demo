package kafka;

import java.util.Map;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.CannotProvideCoderException;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.NullableCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.coders.VarIntCoder;
import org.apache.beam.sdk.io.BoundedReadFromUnboundedSource;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Impulse;
import org.apache.beam.sdk.transforms.Latest;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.transforms.DoFn.Element;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.GlobalWindows;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Read<K, V> extends PTransform<PBegin, PCollection<KV<K, V>>> {
  private static Logger logger = LoggerFactory.getLogger(Read.class);

  private Options<K, V> options;

  private Read(Options<K, V> options) {
    this.options = options;
  }

  @SuppressWarnings("unchecked")
  Coder<KV<K, V>> getCoders(CoderRegistry coderRegistry)
      throws NoSuchMethodException, SecurityException, CannotProvideCoderException {
    Class<K> keyClass = (Class<K>) options.keyDeserializer()
        .getMethod("deserialize", String.class, byte[].class).getReturnType();
    Class<V> valueClass = (Class<V>) options.valueDeserializer()
        .getMethod("deserialize", String.class, byte[].class).getReturnType();
    Coder<K> keyCoder = coderRegistry.getCoder(keyClass);
    Coder<V> valueCoder = coderRegistry.getCoder(valueClass);
    return NullableCoder.of(KvCoder.of(NullableCoder.of(keyCoder), NullableCoder.of(valueCoder)));
  }

  public static <K, V> Read<K, V> withOptions(Options<K, V> options) {
    return new Read<K, V>(options);
  }

  @Override
  public PCollection<KV<K, V>> expand(PBegin input) {
    Pipeline pipeline = input.getPipeline();
    CoderRegistry coderRegistry = pipeline.getCoderRegistry();
    try {
      var kafkaRecords =
          pipeline.apply(Impulse.create()).apply(ParDo.of(new TopicReader<K, V>(options)))
              .setCoder(KvCoder.of(StringUtf8Coder.of(), VarIntCoder.of()))
              .apply(ParDo.of(new BoundedPartitionReader<K, V>(options)))
              .setCoder(getCoders(coderRegistry));
              var t=new BoundedReadFromUnboundedSource<KV<K,V>>(kafkaRecords, 0, null);
      var count = kafkaRecords.apply(Window.<KV<K, V>>into(new GlobalWindows())
          .triggering(Repeatedly.forever(AfterProcessingTime.pastFirstElementInPane()))
          .discardingFiredPanes()).apply(Count.globally())
      /* .apply(Window.<Long>into(new GlobalWindows())
          .triggering(Repeatedly.forever(AfterProcessingTime.pastFirstElementInPane()))
          .discardingFiredPanes())
      .apply(Latest.globally()) */

      /* .apply(View.asSingleton()) */;
      count.apply(ParDo.of(new DoFn<Long, Void>() {
        @ProcessElement
        public void processElement(@Element Long count) {
          logger.info("COUNT: {}", count);
        }
      }));
      return kafkaRecords;
    } catch (Exception e) {
      logger.error("", e);
      return null;
    }
  }
}
