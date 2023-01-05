package dedup;

import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.Validation.Required;

public interface AppPipelineOptions extends PipelineOptions {
    @Description("Path of the baseline file to read from")
    String getBaselineFile();

    void setBaselineFile(String value);

    @Description("Path of the candidate file to read from")
    String getCandidateFile();

    void setCandidateFile(String value);

    @Description("Path of the file to write to")
    @Required
    String getOutputFile();

    void setOutputFile(String value);
}