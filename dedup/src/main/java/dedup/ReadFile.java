package dedup;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.Compression;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;

public class ReadFile extends PTransform<PBegin, PCollection<String[]>> {
  private String header;
  private String inputFile;

  public ReadFile(String header, String inputFile) {
    this.header = header;
    this.inputFile = inputFile;
  }

  @Override
  public PCollection<String[]> expand(PBegin input) {
    return input.apply("Read lines", TextIO.read().from(inputFile).withCompression(Compression.GZIP))
        .apply("Filter headers", Filter.by((String line) -> !line.equals(header)))
        .apply("Split columns",
            MapElements.into(TypeDescriptor.of(String[].class))
                .via((String line) -> line.split(",")))
        .setCoder(AvroCoder.of(TypeDescriptor.of(String[].class)));
  }
}
