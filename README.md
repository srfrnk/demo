# demo

## Setup

1. Install latest [golang](https://go.dev/doc/install)
1. Install latest [kind](https://kind.sigs.k8s.io/): `go install sigs.k8s.io/kind@latest`
1. Install latest [cdk8s](npm install -g cdk8s-cli)
1. Install latest [helm](https://helm.sh/docs/intro/install/)
1. Install latest [k9s](https://github.com/srfrnk/k9s)
1. Install latest [kafkacat](https://github.com/edenhill/kcat)
1. Install latest `typescript`: `npm install typescript -g`
1. Install latest `gradle`: `brew install gradle` or `brew upgrade gradle`
1. Run `make setup`
1. **IMPORTANT** If you're being tesco VPN - run `make load-images` in a second terminal to allow the previous step to complete

## Deploy

1. Run `make deploy`

## Demos

1. Run `k9s` or Manually setup port-forwards to services
1. Open browser at [Flink UI](http://localhost:8081) and [AKHQ](http://localhost:8080)
1. Input and output file will be found in `data` folder

### Generate data

1. Open terminal in `beam-generate` folder
1. Run `gradle run`

### Kafka Streams

#### Push input

1. A kafka connector pod should already be running is "Deploy" was done.
1. Open terminal in root folder
1. Run `make kafka-connect-push-input`

#### Enrich topic -> topic

1. Open terminal in `kstreams` folder
1. Run `gradle run`

#### Pull output

1. A kafka connector pod should already be running is "Deploy" was done.
1. Open terminal in root folder
1. Run `make kafka-connect-pull-output`

### Apache Beam

#### Dedup+Enrich file -> file (No kafka, runs locally)

1. Open terminal in `beam-file2file` folder
1. Run `make direct-runner`

#### Dedup file -> kafka (runs locally or in Flink)

1. Open terminal in `beam-file2kafka` folder

##### Run locally

1. Run `make direct-runner`

##### Run on Flink

1. Setup a [gcloud free account (don't add billing)](https://cloud.google.com/)
1. Run `gcloud auth login`
1. Run `make flinks-runner`

#### Enrich kafka -> kafka (runs locally or in Flink)

1. Open terminal in `beam-kafka2kafka` folder

##### Run locally

1. Run `make direct-runner`

##### Run on Flink

1. Run `make flinks-runner`

#### Aggregate kafka -> file (runs locally)

1. Open terminal in `beam-kafka2file` folder
1. Run `make direct-runner`
