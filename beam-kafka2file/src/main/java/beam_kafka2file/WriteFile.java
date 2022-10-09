package beam_kafka2file;

import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;

public class WriteFile extends PTransform<PCollection<String>, PDone> {
  private String header;
  private String outputFile;

  public WriteFile(String header,String outputFile) {
    this.header = header;
    this.outputFile = outputFile;
  }

  @Override
  public PDone expand(PCollection<String> input) {
    return input.apply(TextIO.write().to(outputFile)
        .withHeader(header).withoutSharding());
  }
}
