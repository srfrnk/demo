package kafka;

import java.io.Serializable;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.serialization.Deserializer;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Options<K, V> implements Serializable {
  public static <K, V> Builder<K, V> builder() {
    return new AutoValue_Options.Builder<>();
  }

  @AutoValue.Builder
  public abstract static class Builder<K, V> {
    public Builder<K, V> withBootstrapServers(String bootstrapServers) {
      return setBootstrapServers(bootstrapServers);
    }

    public Builder<K, V> withTopic(String topic) {
      return setTopic(topic);
    }

    public Builder<K, V> withGroupId(String groupId) {
      return setGroupId(groupId);
    }

    public Builder<K, V> withIsolationLevel(IsolationLevel isolationLevel) {
      return setIsolationLevel(isolationLevel);
    }

    public Builder<K, V> withAutoOffsetResetConfig(OffsetResetStrategy autoOffsetResetConfig) {
      return setAutoOffsetResetConfig(autoOffsetResetConfig);
    }

    public Builder<K, V> withKeyDeserializer(Class<? extends Deserializer<K>> keyDeserializer) {
      return setKeyDeserializer(keyDeserializer);
    }

    public Builder<K, V> withValueDeserializer(Class<? extends Deserializer<V>> valueDeserializer) {
      return setValueDeserializer(valueDeserializer);
    }

    abstract Builder<K, V> setBootstrapServers(String bootstrapServers);

    abstract Builder<K, V> setTopic(String topic);

    abstract Builder<K, V> setGroupId(String groupId);

    abstract Builder<K, V> setIsolationLevel(IsolationLevel isolationLevel);

    abstract Builder<K, V> setAutoOffsetResetConfig(OffsetResetStrategy autoOffsetResetConfig);

    abstract Builder<K, V> setKeyDeserializer(Class<? extends Deserializer<K>> keyDeserializer);

    abstract Builder<K, V> setValueDeserializer(Class<? extends Deserializer<V>> valueDeserializer);

    public abstract Options<K, V> build();
  }

  abstract String bootstrapServers();

  abstract String topic();

  abstract String groupId();

  abstract IsolationLevel isolationLevel();

  abstract OffsetResetStrategy autoOffsetResetConfig();

  abstract Class<? extends Deserializer<K>> keyDeserializer();

  abstract Class<? extends Deserializer<V>> valueDeserializer();

}
