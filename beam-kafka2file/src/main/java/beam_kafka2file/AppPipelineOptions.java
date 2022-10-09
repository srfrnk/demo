package beam_kafka2file;

import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;

public interface AppPipelineOptions extends PipelineOptions {
  @Description("Path of the file to write to")
  String getOutputFile();

  void setOutputFile(String value);
}