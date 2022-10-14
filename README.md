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

## To access gcloud

1. Setup a [gcloud free account (don't add billing)](https://cloud.google.com/)
1. Run `gcloud auth login`
