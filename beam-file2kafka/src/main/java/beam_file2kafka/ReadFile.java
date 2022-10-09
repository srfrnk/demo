package beam_file2kafka;

import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;

public class ReadFile extends PTransform<PBegin, PCollection<String>> {
  private String header;
  private String inputFile;

  public ReadFile(String header,String inputFile) {
    this.header = header;
    this.inputFile = inputFile;
  }

  @Override
  public PCollection<String> expand(PBegin input) {
    return input.apply("Read lines", TextIO.read().from(inputFile))
        .apply("Filter headers", Filter.by((String line) -> !line.equals(header)));
  }
}
