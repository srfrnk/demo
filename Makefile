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
	k9s

deploy:
