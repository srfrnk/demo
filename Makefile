setup:
	- kind delete cluster -n demo
	kind create cluster --config=cluster-config.yaml --image kindest/node:v1.24.0
	kubectl create namespace kafka
	kubectl create namespace couchbase
	kubectl create -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
	kubectl apply -f https://strimzi.io/examples/latest/kafka/kafka-persistent-single.yaml -n kafka
	kubectl wait kafka/my-cluster --for=condition=Ready --timeout=3000s -n kafka
	helm repo add couchbase https://couchbase-partners.github.io/helm-charts/
	helm repo update
	helm install -n couchbase couchbase --set cluster.name=couchbase couchbase/couchbase-operator --version 2.3 --set cluster.security.password='Administrator' --set cluster.security.password='password'
	kubectl wait deployment/couchbase-couchbase-operator --for=condition=Available --timeout=3000s -n couchbase
	kubectl run -n couchbase --attach --rm --restart=Never -i --image yauritux/busybox-curl busybox --command -- sh -c "while ! curl http://couchbase-couchbase-operator:8080 2>/dev/null; do sleep 1; done"
	kubectl apply -f couchbase-config.yaml -n couchbase
	while ! kubectl wait pod/couchbase-0000 --for=condition=Ready --timeout=3000s -n couchbase 2>/dev/null; do sleep 1; done
	kubectl annotate -n couchbase pod/couchbase-0000 k9scli.io/auto-port-forwards=couchbase-server::8091:8091
	kubectl annotate -n kafka pod/my-cluster-kafka-0 k9scli.io/auto-port-forwards=kafka::9092:9092

	@echo "\033[0;33m"
	@echo "**************************************************************************************************************************************"
	@echo "Make sure your '/etc/hosts' file contains:"
	@echo "127.0.0.1 my-cluster-kafka-0.my-cluster-kafka-brokers.kafka.svc"
	@echo "**************************************************************************************************************************************"
	@echo
	@echo "**************************************************************************************************************************************"
	@echo "Make sure you run port forwards for couchbase kafka and enrich-api (Or use K9S for that)"
	@echo "**************************************************************************************************************************************"
	@echo "\033[0"

load-images:
	docker pull quay.io/strimzi/operator:0.31.1
	kind load docker-image -n demo quay.io/strimzi/operator:0.31.1
	docker pull quay.io/strimzi/kafka:0.31.1-kafka-3.2.3
	kind load docker-image -n demo quay.io/strimzi/kafka:0.31.1-kafka-3.2.3

deploy:
	$(eval BUILD = $(shell openssl rand -hex 20;))
	docker build -t enrich-api:${BUILD} enrich-api
	kind -n demo load docker-image enrich-api:${BUILD}
	cd manifests; npm i; cdk8s import; npm run compile; BUILD=${BUILD} cdk8s synth
	kubectl apply -f manifests/dist/manifests.k8s.yaml

clear-data:
	- kubectl exec -n kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic demo
	- kubectl exec -n kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic demo_enriched
	- rm data/output_file2file.csv
	- rm data/output_kafka2file.csv
