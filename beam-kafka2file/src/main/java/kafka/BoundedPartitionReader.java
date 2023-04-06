package kafka;

import org.apache.beam.sdk.transforms.DoFn.*;

@BoundedPerElement
class BoundedPartitionReader<K, V> extends PartitionReader<K,V> {

  BoundedPartitionReader(Options<K, V> options) {
    super(options);
  }
}
