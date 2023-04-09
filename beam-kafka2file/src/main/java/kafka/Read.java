package kafka;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.CannotProvideCoderException;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.NullableCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.coders.VarIntCoder;
import org.apache.beam.sdk.transforms.Impulse;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
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
      return pipeline.apply(Impulse.create()).apply(ParDo.of(new TopicReader<K, V>(options)))
          .setCoder(KvCoder.of(StringUtf8Coder.of(), VarIntCoder.of()))
          .apply(ParDo.of(new BoundedPartitionReader<K, V>(options)))
          .setCoder(getCoders(coderRegistry));
    } catch (Exception e) {
      logger.error("", e);
      return null;
    }
  }
}
