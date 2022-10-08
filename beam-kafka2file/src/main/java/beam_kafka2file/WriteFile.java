package beam_kafka2file;

import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;

public class WriteFile extends PTransform<PCollection<String>, PDone> {
  private String header;

  public WriteFile(String header) {
    this.header = header;
  }

  @Override
  public PDone expand(PCollection<String> input) {
    return input.apply(TextIO.write().to("../data/output_kafka2file.csv")
        .withHeader(header).withoutSharding());
  }
}
