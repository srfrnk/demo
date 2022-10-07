package beam_kafka2file;

import org.apache.avro.generic.GenericData.Fixed;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
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
    return input
        .apply(Window.<String>into(FixedWindows.of(Duration.standardSeconds(60)))
            .triggering(AfterWatermark.pastEndOfWindow().withEarlyFirings(
                AfterProcessingTime.pastFirstElementInPane().plusDelayOf(Duration.standardSeconds(60))))
            .withAllowedLateness(Duration.ZERO).discardingFiredPanes())
        .apply(TextIO.write().to("../data/output_kafka2file_").withSuffix(".csv").withHeader(header)
            .withWindowedWrites().withNumShards(1));
  }
}
