# Simple Producer
This is a simple client, that sends message to a queue.

## How to run

```bash
mvn clean package
java -jar target/http-producer.jar
```

Configure broker settings in [application.properties](src/main/resources/application.properties). 