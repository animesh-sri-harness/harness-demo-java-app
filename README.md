# Harness Demo Java App

A production-shaped Spring Boot web application for testing Harness CI, CD, STO, blue-green,
and canary pipelines. The application exposes its version, release color, environment, and
hostname so that traffic switching and weighted rollout behavior are easy to verify.

## Included

- Java 21 and Spring Boot 3.5
- Maven build with unit/integration tests, JUnit XML, JaCoCo coverage, and Checkstyle
- CycloneDX SBOM generation during `package`
- Optional SpotBugs and OWASP Dependency-Check security profile
- Actuator liveness/readiness probes and Prometheus metrics
- Multi-stage, non-root Docker image and local Docker Compose workflow
- Hardened Kubernetes/Kustomize manifests
- Helm chart with primary/stage services for Harness blue-green deployments
- ECS Fargate task definition and CodeDeploy AppSpec example
- Harness v0 CI/CD/STO pipeline template with canary and blue-green stages

## Build and run

Prerequisites are Java 21 (the Maven wrapper is included), or Docker.

```bash
make verify
java -jar target/app.jar
```

Without a local JDK:

```bash
docker compose up --build --wait
./scripts/smoke-test.sh http://localhost:8080 local blue
docker compose down
```

Useful endpoints:

- Web UI: `http://localhost:8080/`
- Release metadata: `http://localhost:8080/api/v1/info`
- Greeting API: `http://localhost:8080/api/v1/greetings`
- Readiness: `http://localhost:8080/actuator/health/readiness`
- Liveness: `http://localhost:8080/actuator/health/liveness`
- Prometheus: `http://localhost:8080/actuator/prometheus`

Deployment metadata is controlled with `APP_VERSION`, `RELEASE_COLOR`, `APP_ENVIRONMENT`, and
`HOSTNAME`. Set different version/color values for stable and candidate releases, then repeatedly
call `/api/v1/info` to observe canary distribution or a blue-green traffic swap.

## Build outputs for CI and STO

Run the normal quality gate:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

This produces:

- Test reports in `target/surefire-reports/`
- HTML and XML coverage in `target/site/jacoco/`
- CycloneDX SBOM at `target/bom.json`
- Executable application at `target/app.jar`

Run local software-composition and static analysis:

```bash
NVD_API_KEY=... ./mvnw --batch-mode --no-transfer-progress verify -Psecurity
```

The NVD key is optional but strongly recommended to avoid public API throttling. Harness STO should
use native scanner steps; the supplied pipeline runs Harness Code SAST/SCA and Gitleaks so findings
appear in the Security Tests view.

## Containers

```bash
docker build \
  --build-arg APP_VERSION=1.0.0 \
  --build-arg VCS_REF="$(git rev-parse HEAD)" \
  -t registry.example.com/team/harness-demo-java-app:1.0.0 .
docker run --rm -p 8080:8080 \
  -e APP_VERSION=1.0.0 \
  -e RELEASE_COLOR=green \
  -e APP_ENVIRONMENT=staging \
  registry.example.com/team/harness-demo-java-app:1.0.0
```

The image runs as UID `10001`, uses a read-only-compatible filesystem, and honors
`JAVA_TOOL_OPTIONS` for runtime tuning.

## Kubernetes and Helm

Render or apply the plain manifests:

```bash
kubectl kustomize deploy/kubernetes/base
kubectl apply -k deploy/kubernetes/base
kubectl -n harness-demo port-forward service/harness-demo-java-app 8080:80
```

Override the image for a deployment:

```bash
cd deploy/kubernetes
kustomize edit set image \
  harness-demo-java-app=registry.example.com/team/harness-demo-java-app:1.0.0
```

Render and test the chart:

```bash
helm lint deploy/helm/harness-demo-java-app
helm upgrade --install demo deploy/helm/harness-demo-java-app \
  --namespace harness-demo --create-namespace \
  --set image.repository=registry.example.com/team/harness-demo-java-app \
  --set image.tag=1.0.0 \
  --set app.version=1.0.0 \
  --set app.releaseColor=green
helm test demo --namespace harness-demo
```

Both Kubernetes packaging options define Harness primary and stage service annotations. Use the
native `K8sCanaryDeploy`/`K8sCanaryDelete` steps for canary, or
`K8sBlueGreenDeploy`/`K8sBGSwapServices` for blue-green. Set `RELEASE_COLOR` through a service
variable or Helm value to distinguish candidate traffic.

## AWS ECS

`deploy/ecs/task-definition.json` is designed for a Harness ECS service:

- `<+artifact.image>` and `<+artifact.tag>` resolve from the service artifact.
- `executionRoleArn`, `taskRoleArn`, and `releaseColor` are Harness service variables.
- The container health check calls the Spring Boot readiness endpoint.

For rolling delivery, use the native `EcsRollingDeploy` step. For progressive delivery, configure
two ALB target groups in the Harness ECS infrastructure and use
`EcsBlueGreenCreateService` plus `EcsBlueGreenSwapTargetGroups`, or `EcsCanaryDeploy`.
`deploy/ecs/appspec.yaml` is also provided for teams using AWS CodeDeploy directly; replace its
network placeholders in a rendered copy, not in the reusable source template.

## Harness pipeline

The portable pipeline is `.harness/pipeline.yaml.template`. It performs:

1. Maven build, tests, coverage gate, and SBOM generation.
2. Parallel Harness Code SAST/SCA and Gitleaks scans.
3. Native Docker build and registry push.
4. A selected Kubernetes canary or blue-green deployment with approval and rollback.

Render only the Harness scope fields before importing:

```bash
export HARNESS_ORG_ID=your_org
export HARNESS_PROJECT_ID=your_project
envsubst '${HARNESS_ORG_ID} ${HARNESS_PROJECT_ID}' \
  < .harness/pipeline.yaml.template > /tmp/harness-demo-pipeline.yaml
```

In Harness, configure these dependencies before importing the pipeline:

1. A Git connector for this repository.
2. A Docker Registry connector and image repository.
3. A Kubernetes service using either `deploy/kubernetes/base` or the Helm chart.
4. An environment and Kubernetes infrastructure definition.
5. An STO-enabled project and an approval user group.

The remaining connector, repository, service, environment, infrastructure, artifact, and strategy
fields are Harness runtime inputs. Use `canary` or `bluegreen` for `deploymentStrategy`.

For a Native Helm service, keep the CI stage and replace the selected deployment stage with a
native `HelmDeploy`/`HelmRollback` stage. For ECS, use the task definition above and the native ECS
steps described in the previous section. Cloud account, region, cluster, registry, and target-group
identifiers are intentionally not committed because they are environment-specific and should be
selected through Harness connectors and infrastructure definitions.

## Progressive delivery smoke test

After deployment, verify a specific candidate:

```bash
./scripts/smoke-test.sh https://app.example.com 1.1.0 green
```

For canary, run the request repeatedly and count the `version` or `color` values. For blue-green,
test the stage service before approval and the primary service after the swap.