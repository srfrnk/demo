package beam_file2file;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;

public class ReadFile extends PTransform<PBegin, PCollection<KV<String, String>>> {
  private String header;

  public ReadFile(String header) {
    this.header = header;
  }

  @Override
  public PCollection<KV<String, String>> expand(PBegin input) {
    return input.apply("Read lines", TextIO.read().from("../data/input.csv"))
        .apply("Filter headers", Filter.by((String line) -> !line.equals(header)))
        .apply("Split columns",
            MapElements.into(TypeDescriptor.of(String[].class))
                .via((String line) -> line.split(",")))
        .setCoder(AvroCoder.of(TypeDescriptor.of(String[].class))).apply("Split to KV",
            MapElements
                .into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.strings()))
                .via((String[] cols) -> KV.of(cols[0], cols[1])));
  }
}
