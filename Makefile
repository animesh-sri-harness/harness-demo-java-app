APP_NAME := harness-demo-java-app
IMAGE_TAG ?= local
CHART := deploy/helm/harness-demo-java-app
MVN ?= ./mvnw

.PHONY: test package verify security docker-build compose-up compose-down \
	k8s-render k8s-apply helm-lint helm-template

test:
	$(MVN) --batch-mode --no-transfer-progress test

package:
	$(MVN) --batch-mode --no-transfer-progress package

verify:
	$(MVN) --batch-mode --no-transfer-progress clean verify

security:
	$(MVN) --batch-mode --no-transfer-progress verify -Psecurity

docker-build:
	docker build --build-arg APP_VERSION=$(IMAGE_TAG) -t $(APP_NAME):$(IMAGE_TAG) .

compose-up:
	docker compose up --build --wait

compose-down:
	docker compose down

k8s-render:
	kubectl kustomize deploy/kubernetes/base

k8s-apply:
	kubectl apply -k deploy/kubernetes/base

helm-lint:
	helm lint $(CHART)

helm-template:
	helm template $(APP_NAME) $(CHART)
