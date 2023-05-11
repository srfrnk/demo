direct-runner:
	gradle run --args " \
	--outputFile=../data/output_kafka2file.csv \
	"

flink-local-runner:
	gradle run -Dlog.file=/tmp/log --args " \
	--runner=FlinkRunner \
	--flinkMaster=[local] \
	--fasterCopy \
	--disableMetrics \
	--parallelism=3 \
	--outputFile=../data/output_kafka2file.csv \
	"
