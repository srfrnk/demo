package kafka;

import org.apache.beam.sdk.transforms.DoFn.*;


@UnboundedPerElement
class UnboundedPartitionReader<K, V> extends PartitionReader<K, V> {

  UnboundedPartitionReader(Options<K, V> options) {
    super(options);
  }
}
