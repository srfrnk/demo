package dedup;

import java.util.Arrays;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.transforms.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static Logger logger = LoggerFactory.getLogger(App.class);
    private static String SCHEMA = "basket_id,quantity,tpnb,regular_product_price,total_price_with_discount,ad_id,order_timestamp";

    public static void main(String[] args) {
        AppPipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
                .as(AppPipelineOptions.class);
        Pipeline pipeline = Pipeline.create(options);
        PCollection<String[]> linesBaseline = pipeline.apply(SCHEMA, new ReadFile(SCHEMA, options.getBaselineFile()));
        PCollection<String[]> linesCandidate = pipeline.apply(SCHEMA, new ReadFile(SCHEMA, options.getCandidateFile()));
        linesCandidate.apply(Count.globally()).apply(ParDo.of(new DoFn<Long, Void>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                Long count = c.element();
                logger.info("Total Candidate Lines: {}", count);
            }
        }));

        PCollection<KV<String, String[]>> parsedBaseline = linesBaseline.apply(MapElements
                .into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptor.of(String[].class)))
                .via((String[] cols) -> {
                    return KV.of(cols[0], cols);
                }))
                .setCoder(KvCoder.of(AvroCoder.of(TypeDescriptors.strings()),
                        AvroCoder.of(TypeDescriptor.of(String[].class))));

        PCollection<KV<String, String[]>> parsedCandidate = linesCandidate.apply(MapElements
                .into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptor.of(String[].class)))
                .via((String[] cols) -> {
                    return KV.of(cols[0], cols);
                }))
                .setCoder(KvCoder.of(AvroCoder.of(TypeDescriptors.strings()),
                        AvroCoder.of(TypeDescriptor.of(String[].class))));

        TupleTag<String[]> baselineTag = new TupleTag<>();
        TupleTag<String[]> candidateTag = new TupleTag<>();

        PCollection<KV<String, CoGbkResult>> tuple = KeyedPCollectionTuple
                .of(candidateTag, parsedCandidate)
                .and(baselineTag, parsedBaseline)
                .apply(CoGroupByKey.create());

        PCollection<String[]> linesOutput = tuple
                .apply(ParDo.of(new DoFn<KV<String, CoGbkResult>, String[]>() {
                    @ProcessElement
                    public void processElement(ProcessContext c) {
                        CoGbkResult group = c.element().getValue();
                        if (!group.getAll(baselineTag).iterator().hasNext()
                                && group.getAll(candidateTag).iterator().hasNext()) {
                            c.output(group.getAll(candidateTag).iterator().next());
                        }
                    }
                }))
                .setCoder(AvroCoder.of(TypeDescriptor.of(String[].class)));

        linesOutput.apply(Count.globally()).apply(ParDo.of(new DoFn<Long, Void>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                Long count = c.element();
                logger.info("Total Output: {}", count);
            }
        }));

        linesOutput.apply(new WriteFile(SCHEMA, options.getOutputFile()));
        pipeline.run();
    }
}
