package beam_kafka2file;

import org.apache.avro.generic.GenericData.Fixed;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.*;
import org.joda.time.Duration;

public class WriteFile extends PTransform<PCollection<String>, PDone> {
  private String header;

  public WriteFile(String header) {
    this.header = header;
  }

  @Override
  public PDone expand(PCollection<String> input) {
    return input.apply(Window.into(FixedWindows.of(Duration.standardSeconds(30))))
        .apply(TextIO.write().to("../data/output_kafka2file.csv").withoutSharding()
            .withHeader(header).withWindowedWrites());
  }
}
