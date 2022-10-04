package beam_generate;

import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.*;

public class WriteFile extends PTransform<PCollection<String[]>, PDone> {
  private String header;

  public WriteFile(String header) {
    this.header = header;
  }

  @Override
  public PDone expand(PCollection<String[]> input) {
    return input
        .apply("Merge columns",
            MapElements.into(TypeDescriptors.strings())
                .via((String[] cols) -> String.join(",", cols)))
        .apply(TextIO.write().to("../data/input.csv").withoutSharding().withHeader(header));
  }
}
