# Producer
This is a simple client, that sends JSON messages to a configurable queue

## How to run

```bash
mvn clean package
java -jar target/producer.jar
```

Configure broker settings in [application.properties](src/main/resources/application.properties). 