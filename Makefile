IMAGE_BUILD=false
IMAGE_PUSH=false
REUSE_EXISTING=false
IMAGE_REGISTRY=quay.io
IMAGE_ARCH=linux/amd64,linux/arm64/v8
CONTAINER_RUNTIME=podman

build:
	@echo ">> building binaries"
	mvn package \
      -Dnative \
      -DskipTests \
      -Dquarkus.native.container-build=true \
      -Dquarkus.native.container-runtime=$(CONTAINER_RUNTIME) \
      -Dquarkus.jib.platforms=$(IMAGE_ARCH) \
      -Dquarkus.container-image.build=$(IMAGE_BUILD) \
      -Dquarkus.container-image.push=$(IMAGE_PUSH) \
      -Dquarkus.container-image.registry=$(IMAGE_REGISTRY) \
      -Dquarkus.native.reuse-existing=$(REUSE_EXISTING)

clean:
	@echo ">> Clean target"
	mvn clean

verify:
	@echo ">> Verify build"
	mvn test-compile failsafe:integration-test -Dnative

test:
	@echo ">> run unit test"
	mvn test

image: IMAGE_BUILD=true
image: REUSE_EXISTING=true
image: build

push: IMAGE_PUSH=true
push: image


up:
	@echo ">> startup compose"
	podman-compose -f compose.yml up -d

perf-test:
	@echo ">> run perf test"
	podman-compose -f compose.yml restart producer-perf-test

down:
	@echo ">> shutdown compose"
	podman-compose -f compose.yml down
