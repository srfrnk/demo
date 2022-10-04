package _file2file;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Distinct;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  private static final String HEADER_INPUT = "name,query";
  private static final String HEADER_OUTPUT = "name,results";
  private static Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
    Pipeline pipeline = Pipeline.create(options);
    PCollection<KV<String, String>> linesInput = pipeline.apply(new ReadFile(HEADER_INPUT));

    PCollection<String[]> linesOutput = linesInput.apply("Dedup", Distinct.create())
        .apply("Enrich data", ParDo.of(new DoFn<KV<String, String>, KV<String, String>>() {
          @ProcessElement
          public void processElement(ProcessContext c) {
            KV<String, String> kv = c.element();
            String name = kv.getKey();
            String query = kv.getValue();

            // logger.info("** {} Start: {}", name, query);
            String results = SomeClient.callAPI(query);
            // logger.info("** {} End: {}", name, results);

            c.output(KV.of(name, results));
          }
        }))
        .apply("Map to cols",
            MapElements.into(TypeDescriptor.of(String[].class))
                .via((KV<String, String> kv) -> new String[] {kv.getKey(), kv.getValue()}))
        .setCoder(AvroCoder.of(TypeDescriptor.of(String[].class)));

    linesOutput.apply(new WriteFile(HEADER_OUTPUT));
    pipeline.run();
  }
}
