# Wolt assignment 2024 🚀
This is my solution for [Wolt Internship assignment 2024](https://github.com/woltapp/engineering-internship-2024) using Kotlin Spring boot.
Previously I did the same assignment using FastAPI and this time I did it using Kotlin Spring Boot.

## How to run
1. Make sure you have Java 21, docker, Make and Git installed
2. Clone the repository
3. Have Java 21 and docker installed
4. Run the following command
```shell
make  // This will build the project using gradle, build the docker image and run the server.
```
5. The server will be running on `localhost:8080`
6. You can test the API using the Postman app or the following curl command
```shell
curl -X POST http://localhost:8080/cart -H "Content-Type: application/json" -d "{\"cart_value\": 720, \"delivery_distance\": 2235, \"number_of_items\": 4, \"time\": \"2024-01-15T13:00:00Z\"}"
```
Should return
```json
{
    "delivery_fee": 780
}
```

## How to test


## How to stop
1. Run the following command
```shell
make down // This will stop the server and remove the container
make clean // This will remove the docker image
```