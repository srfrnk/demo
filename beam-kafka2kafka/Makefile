direct-runner:
	gradle run

flink-runner:
	gradle build run --args " \
	--runner=FlinkRunner \
	--flinkMaster=flink-jobmanager.flink.svc:8081 \
	--filesToStage=build/libs/app.jar \
	--parallelism=3 \
	"
