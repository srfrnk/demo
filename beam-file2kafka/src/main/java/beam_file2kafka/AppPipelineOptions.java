package beam_file2kafka;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;

public interface AppPipelineOptions extends PipelineOptions {
  @Description("Path of the file to read from")
  @Default.String("gs://apache-beam-samples/shakespeare/kinglear.txt")
  String getInputFile();

  void setInputFile(String value);
}