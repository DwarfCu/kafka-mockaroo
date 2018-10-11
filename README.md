# Mockaroo Kafka Producer

This project tries to simulate streaming events due to many times at many projects the dataset is not available while we are starting to develop,  or is not complete or, worst, simply it doesn't exist.

[Mockaroo](https://mockaroo.com/) let you to create an API to get events from a schema that you have defined previously. Then, a Kafka Producer calls your Mockaroo API and pushes the events to a *topic*. Several Kafka Producers can be running at the same time.

Events are checked against an schema thanks to **Confluent Schema Registry** service.

### [HowTo] Use it 
 
1. Create a schema and API at [Mockaroo](https://mockaroo.com/). For example:

    BankBalanceDataset: https://mockaroo.com/9b0d87e0

2. Setup ***resources/mockaroo.properties***. For example:
  
       mockaroo.url=https://api.mockaroo.com/api/9b0d87e0?count=1000&key=<your_key>

3. Create the right AVRO Schema ***resources/avro/avro-schema.avsc***. Please, edit only the *fields* section. For BankBalanceDataset example:

       {
         "type": "record",
         "namespace": "com.dwarfcu.mockaroo",
         "name": "Dataset",
         "version": "1",
         "fields": [
           { "name": "name", "type": "string", "doc": "First Name of customer" },
           { "name": "amount", "type": "string", "doc": "Amount of money between [0..100]" },
           { "name": "date", "type": "string", "doc": "Date of transaction" },
           { "name": "time", "type": "string", "doc": "Time of transaction" }
         ]
       }

4. Setup ***resources/kafka.properties*** according to your Confluent-Kafka environment. For BankBalanceDataset example:
 
       BOOTSTRAP_SERVERS_CONFIG=172.18.0.3:9092
       SCHEMA_REGISTRY_URL_CONFIG=http://172.18.0.4:8081
       CLIENT_ID_CONFIG=MockarooBankBalanceDatasetProducer
       TOPIC=mockarooBankBalanceDataset
       
5. Maven project.

       bash$ mvn clean package

6. Run **MockarooProducer** class.

#

#### [HowTo] Deploy a Test Environment with Docker and docker-compose.

1. Create *mockaroo* directory.

       bash$ mkdir mockaroo

2. Create **docker-compose.yaml**

       bash$ cd mockaroo
       bash$ vim docker-compose.yaml

````
version: "2"
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:5.0.0
    environment:
      - ZOOKEEPER_CLIENT_PORT=2181
    ports:
      - "2181:2181"
    networks:
      confluent:
        ipv4_address: 172.18.0.2

  kafka:
    image: confluentinc/cp-kafka:5.0.0
    depends_on:
      - zookeeper
    environment:
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://172.18.0.3:9092
      - KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
    ports:
      - "9092:9092"
    networks:
      confluent:
        ipv4_address: 172.18.0.3

  schema-registry:
    image: confluentinc/cp-schema-registry:5.0.0
    depends_on:
      - kafka
    environment:
      - SCHEMA_REGISTRY_HOST_NAME=schema-registry
      - SCHEMA_REGISTRY_LISTENERS=http://0.0.0.0:8081
      - SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL=zookeeper:2181
    ports:
      - "8081:8081"
    networks:
      confluent:
        ipv4_address: 172.18.0.4

networks:
  confluent:
    driver: bridge
    ipam:
      driver: default
      config:
        - subnet: 172.18.0.0/24
          gateway: 172.18.0.1
````

Run docker containers.

       bash$ docker-compose up

[+INFO] [Confluent: Single Node Basic Deployment on Docker](https://docs.confluent.io/current/installation/docker/docs/installation/single-node-client.html)
