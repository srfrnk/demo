package kafka;

import org.apache.beam.sdk.transforms.DoFn.BoundedPerElement;

@BoundedPerElement
class BoundedPartitionReader<K, V> extends UnboundedPartitionReader<K, V> {
  BoundedPartitionReader(Options<K, V> options) {
    super(options);
  }
}
