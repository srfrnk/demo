setup:
	- kind delete cluster -n demo
	kind create cluster --config=cluster-config.yaml --image kindest/node:v1.24.0
	kubectl create namespace kafka
	kubectl create namespace couchbase
	kubectl create namespace flink

	kubectl create -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
	kubectl apply -f https://strimzi.io/examples/latest/kafka/kafka-persistent-single.yaml -n kafka
	kubectl wait kafka/my-cluster --for=condition=Ready --timeout=3000s -n kafka

	# helm repo add couchbase https://couchbase-partners.github.io/helm-charts/
	# helm repo update
	# helm install -n couchbase couchbase --set cluster.name=couchbase couchbase/couchbase-operator --version 2.3 --set cluster.security.password='Administrator' --set cluster.security.password='password'
	# kubectl wait deployment/couchbase-couchbase-operator --for=condition=Available --timeout=3000s -n couchbase
	# kubectl run -n couchbase --attach --rm --restart=Never -i --image yauritux/busybox-curl busybox --command -- sh -c "while ! curl http://couchbase-couchbase-operator:8080 2>/dev/null; do sleep 1; done"
	# kubectl apply -f couchbase-config.yaml -n couchbase
	# while ! kubectl wait pod/couchbase-0000 --for=condition=Ready --timeout=3000s -n couchbase 2>/dev/null; do sleep 1; done

	kubectl apply -f flink-manifests.yaml
	kubectl wait deployment/flink-jobmanager --for=condition=Available --timeout=3000s -n flink

	helm repo add akhq https://akhq.io/
	helm upgrade -n kafka --install akhq akhq/akhq
	kubectl apply -f akhq-config.yaml
	kubectl wait deployment/akhq --for=condition=Available --timeout=3000s -n kafka

	# kubectl annotate -n couchbase pod/couchbase-0000 k9scli.io/auto-port-forwards=couchbase-server::8091:8091
	kubectl annotate -n kafka pod/my-cluster-kafka-0 k9scli.io/auto-port-forwards=kafka::9092:9092
	kubectl annotate -n kafka pod -l app.kubernetes.io/name=akhq k9scli.io/auto-port-forwards=akhq::8080:8080

	- mkdir data

	@echo "\033[0;33m"
	@echo "**************************************************************************************************************************************"
	@echo "Make sure your '/etc/hosts' file contains:"
	@echo "127.0.0.1 my-cluster-kafka-0.my-cluster-kafka-brokers.kafka.svc"
	@echo "127.0.0.1 flink-jobmanager.flink.svc"
	@echo "127.0.0.1 enrich-api.default.svc"
	@echo "**************************************************************************************************************************************"
	@echo
	@echo "**************************************************************************************************************************************"
	@echo "Make sure you run port forwards for couchbase, kafka, flink, akhq and enrich-api (Or use K9S for that)"
	@echo "**************************************************************************************************************************************"
	@echo "\033[0"

load-images:
	curl -sL "https://strimzi.io/install/latest?namespace=kafka" > /tmp/strimzi_manifest.yaml	
	IMG_OPERATOR=$$(cat /tmp/strimzi_manifest.yaml | yq 'select(.kind == "Deployment" and .metadata.name== "strimzi-cluster-operator") | .spec.template.spec.containers[0].image') &&\
		docker pull $${IMG_OPERATOR} &&\
		kind load docker-image -n demo $${IMG_OPERATOR}
	IMG_KAFKA=$$(cat /tmp/strimzi_manifest.yaml | yq 'select(.kind == "Deployment" and .metadata.name== "strimzi-cluster-operator") | .spec.template.spec.containers[0].env[] | select(.name=="STRIMZI_DEFAULT_KAFKA_EXPORTER_IMAGE") | .value') &&\
		docker pull $${IMG_KAFKA} &&\
		kind load docker-image -n demo $${IMG_KAFKA}

deploy:
	# Used '--no-cache' when building due to bug with 'kind' not retagging properly

	$(eval BUILD = $(shell openssl rand -hex 20;))
	docker build --no-cache -t enrich-api:${BUILD} enrich-api
	kind -n demo load docker-image enrich-api:${BUILD}
	docker build --no-cache -t kafka-connect:${BUILD} kafka-connect
	kind -n demo load docker-image kafka-connect:${BUILD}

	cd manifests; npm i; cdk8s import; npm run compile; BUILD=${BUILD} cdk8s synth
	kubectl apply -f manifests/dist/manifests.k8s.yaml

clear-data:
	- kubectl exec -n kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic demo
	- kubectl exec -n kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic demo_streams
	- kubectl exec -n kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic demo_enriched
	- kubectl exec -n kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic demo_enriched_streams
	- kubectl exec -n kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic streams-linesplit-output
	- kubectl exec -n kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic streams-wordcount-counts-store-changelog
	- kubectl exec -n kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic demo_copy
	- kubectl exec -n kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic streams-wordcount-output
	- kubectl exec -n kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic streams-wordcount-counts-store-repartition
	- rm data/output_*.csv

kafka-connect-push-input:
	kubectl -n default cp data/input.csv $$(kubectl get -n default pod -l app=kafka-connect -o custom-columns=":metadata.name" --no-headers):/home/kafka/input.csv

kafka-connect-consume-topic-input:
	kafkacat -b my-cluster-kafka-0.my-cluster-kafka-brokers.kafka.svc:9092 -C -t demo_streams

kafka-connect-consume-topic-output:
	kafkacat -b my-cluster-kafka-0.my-cluster-kafka-brokers.kafka.svc:9092 -C -t demo_enriched_streams

kafka-connect-pull-output:
	kubectl -n default cp $$(kubectl get -n default pod -l app=kafka-connect -o custom-columns=":metadata.name" --no-headers):/home/kafka/output.csv data/output_streams.csv

scale-topic:
	- kubectl exec -n kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic demo
	- kubectl exec -n kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --alter --topic demo --partitions 10