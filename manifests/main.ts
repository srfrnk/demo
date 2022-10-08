import { Construct } from 'constructs';
import { App, Chart, ChartProps } from 'cdk8s';
import { IntOrString, KubeDeployment, KubeService } from './imports/k8s';

export class MyChart extends Chart {
  constructor(scope: Construct, id: string, props: ChartProps = {}) {
    super(scope, id, props);
    const labelEnrichApi = { app: 'enrich-api' };

    new KubeService(this, 'enrich-api-service', {
      metadata: {
        name: 'enrich-api'
      },
      spec: {
        type: 'LoadBalancer',
        ports: [{ port: 3000, targetPort: IntOrString.fromNumber(3000) }],
        selector: labelEnrichApi
      }
    });

    new KubeDeployment(this, 'enrich-api-deployment', {
      metadata: {
        name: 'enrich-api'
      },
      spec: {
        replicas: 1,
        selector: {
          matchLabels: labelEnrichApi
        },
        template: {
          metadata: {
            labels: labelEnrichApi,
            annotations: {
              'k9scli.io/auto-port-forwards': 'enrich-api::3000:3000'
            }
          },
          spec: {
            containers: [
              {
                name: 'enrich-api',
                image: `enrich-api:${process.env['BUILD']}`,
                ports: [{ containerPort: 3000 }],
              }
            ]
          }
        }
      }
    });

    const labelKafkaConnect = { app: 'kafka-connect' };

    new KubeDeployment(this, 'kafka-connect-deployment', {
      metadata: {
        name: 'kafka-connect'
      },
      spec: {
        replicas: 1,
        selector: {
          matchLabels: labelKafkaConnect
        },
        template: {
          metadata: {
            labels: labelKafkaConnect,
            annotations: {
              // 'k9scli.io/auto-port-forwards': 'enrich-api::3000:3000'
            }
          },
          spec: {
            containers: [
              {
                name: 'kafka-connect',
                image: `kafka-connect:${process.env['BUILD']}`,
                // ports: [{ containerPort: 3000 }],
              }
            ]
          }
        }
      }
    });
  }
}

const app = new App();
new MyChart(app, 'manifests');
app.synth();
