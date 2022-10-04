package beam_generate;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  private static final String HEADER = "name,query";
  private static Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
    Pipeline pipeline = Pipeline.create(options);


    pipeline.apply(GenerateSequence.from(0).to(10000))
        .apply(MapElements.into(TypeDescriptor.of(String[].class))
            .via((Long idx) -> new String[] {Names.getRandomNames(2), Words.getRandomWords(6)}))
        .setCoder(AvroCoder.of(TypeDescriptor.of(String[].class))).apply(new WriteFile(HEADER));
    pipeline.run();
  }
}
