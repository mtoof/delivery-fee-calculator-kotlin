ifeq ($(OS), Windows_NT)
    GRADLE_CMD = gradlew
else
    GRADLE_CMD = ./gradlew
endif


all: build run

.PHONY: all build run down clean test
build:
	$(GRADLE_CMD) build
	docker compose up -d --build
test:
	$(GRADLE_CMD) test
down:
	docker compose down
clean:
	@if docker image inspect delivery_fee > /dev/null 2>&1; then \
    	docker image rm delivery_fee; \
	else \
    	echo "Image delivery_fee not found."; \
	fi
