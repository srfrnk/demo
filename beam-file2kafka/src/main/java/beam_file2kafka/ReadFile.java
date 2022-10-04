package beam_file2kafka;

import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;

public class ReadFile extends PTransform<PBegin, PCollection<String>> {
  private String header;

  public ReadFile(String header) {
    this.header = header;
  }

  @Override
  public PCollection<String> expand(PBegin input) {
    return input.apply("Read lines", TextIO.read().from("../data/input.csv"))
        .apply("Filter headers", Filter.by((String line) -> !line.equals(header)));
  }
}
