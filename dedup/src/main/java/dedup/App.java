package dedup;

import java.util.Arrays;
import java.util.stream.StreamSupport;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  private static Logger logger = LoggerFactory.getLogger(App.class);
  private static String SCHEMA = "basket_id,quantity,tpnb,regular_product_price,total_price_with_discount,ad_id,order_timestamp";

  public static void main(String[] args) {
    AppPipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(AppPipelineOptions.class);
    Pipeline pipeline = Pipeline.create(options);
    PCollection<String[]> linesInput = pipeline.apply(SCHEMA, new ReadFile(SCHEMA, options.getInputFile()));
    linesInput.apply(Count.globally()).apply(ParDo.of(new DoFn<Long, Void>() {
      @ProcessElement
      public void processElement(ProcessContext c) {
        Long count = c.element();
        logger.info("Total Input: {}", count);
      }
    }));

    PCollection<String[]> linesOutput = linesInput
        .apply(MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptor.of(String[].class)))
            .via((String[] cols) -> {
              return KV.of(String.join(",", Arrays.copyOfRange(cols, 1, cols.length - 1)), cols);
            }))
        .setCoder(KvCoder.of(AvroCoder.of(TypeDescriptors.strings()),
            AvroCoder.of(TypeDescriptor.of(String[].class))))
        .apply("Dedup", GroupByKey.<String, String[]>create())
        .apply(MapElements.into(TypeDescriptor.of(String[].class))
            .via((group) -> {
              return StreamSupport.stream(group.getValue().spliterator(), false).findFirst().get();
            }))
        .setCoder(AvroCoder.of(TypeDescriptor.of(String[].class)));

    linesOutput.apply(Count.globally()).apply(ParDo.of(new DoFn<Long, Void>() {
      @ProcessElement
      public void processElement(ProcessContext c) {
        Long count = c.element();
        logger.info("Total Output: {}", count);
      }
    }));

    linesOutput.apply(new WriteFile(SCHEMA, options.getOutputFile()));
    pipeline.run();
  }
}
