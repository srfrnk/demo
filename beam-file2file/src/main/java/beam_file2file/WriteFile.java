package beam_file2file;

import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.*;

public class WriteFile extends PTransform<PCollection<String[]>, PDone> {
  private String header;
  private String outputFile;

  public WriteFile(String header,String outputFile) {
    this.header = header;
    this.outputFile = outputFile;
  }

  @Override
  public PDone expand(PCollection<String[]> input) {
    return input
        .apply("Merge columns",
            MapElements.into(TypeDescriptors.strings())
                .via((String[] cols) -> String.join(",", cols)))
        .apply(TextIO.write().to(outputFile).withoutSharding().withHeader(header));
  }
}
