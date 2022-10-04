import { Construct } from 'constructs';
import { App, Chart, ChartProps } from 'cdk8s';
import { IntOrString, KubeDeployment, KubeService } from './imports/k8s';

export class MyChart extends Chart {
  constructor(scope: Construct, id: string, props: ChartProps = {}) {
    super(scope, id, props);
    const label = { app: 'enrich-api' };

    new KubeService(this, 'service', {
      metadata: {
        name: 'enrich-api'
      },
      spec: {
        type: 'LoadBalancer',
        ports: [{ port: 3000, targetPort: IntOrString.fromNumber(3000) }],
        selector: label
      }
    });

    new KubeDeployment(this, 'deployment', {
      metadata: {
        name: 'enrich-api'
      },
      spec: {
        replicas: 1,
        selector: {
          matchLabels: label
        },
        template: {
          metadata: {
            labels: label,
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
  }
}

const app = new App();
new MyChart(app, 'manifests');
app.synth();
