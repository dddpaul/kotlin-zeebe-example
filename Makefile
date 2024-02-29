.PHONY: all build

all: build

build:
	@./gradlew clean bootJar

worker:
	@java -jar build/libs/kotlin-zeebe-example-0.0.1-SNAPSHOT.jar --app.worker.enabled=true

worker-bar:
	@LOGGING_LEVEL_COM_GITHUB_DDDPAUL_ZEEBEEXAMPLE_WORKERS=OFF \
	java -jar build/libs/kotlin-zeebe-example-0.0.1-SNAPSHOT.jar \
	--app.worker.enabled=true --app.worker.progress-bar.enabled=true
