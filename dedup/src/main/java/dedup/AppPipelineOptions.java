package dedup;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.Validation.Required;

public interface AppPipelineOptions extends PipelineOptions {
  @Description("Path of the file to read from")
  @Default.String("gs://apache-beam-samples/shakespeare/kinglear.txt")
  String getInputFile();

  void setInputFile(String value);

  @Description("Path of the file to write to")
  @Required
  String getOutputFile();

  void setOutputFile(String value);
}