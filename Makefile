all: build run

.PHONY: all build run down clean
build:
	docker build -t delivery_fee .
run:
	docker run -d -p 8080:8080 --name delivery_fee delivery_fee
down:
	docker stop delivery_fee
clean:
	docker rm delivery_fee
	docker rmi delivery_fee
