package kafka;

import org.apache.beam.sdk.coders.VarLongCoder;
import org.apache.beam.sdk.state.StateSpec;
import org.apache.beam.sdk.state.StateSpecs;
import org.apache.beam.sdk.state.ValueState;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;

class BoundedReader<K, V> extends DoFn<KV<K, V>, KV<K, V>> {
  @StateId("recordsConsumed")
  private final StateSpec<ValueState<Long>> recordsConsumedSpec =
      StateSpecs.value(VarLongCoder.of());

  private Options<K, V> options;

  BoundedReader(Options<K, V> options) {
    this.options = options;
  }

  @ProcessElement
  public void processElement(@Element KV<K, V> input, OutputReceiver<KV<K, V>> output,
      @StateId("recordsConsumed") ValueState<Long> recordsConsumedState) throws Exception {
    Long recordsConsumed = recordsConsumedState.read();
    recordsConsumedState.write(recordsConsumed);
    output.output(input);
  }

}
