package com.dwarfcu.kafka.mockaroo;

import com.dwarfcu.kafka.Dataset;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

public class MockarooProducer {

  private static final Logger logger = LogManager.getLogger(MockarooProducer.class.getName());

  private static Properties properties;

  public static void main(String[] args) {

    logger.info("[KAFKA] Starting...");

    properties = new Properties();
    try {
      properties.load(MockarooData.class.getClassLoader().getResource("kafka.properties").openStream());
      properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, MockarooProducer.get("BOOTSTRAP_SERVERS_CONFIG"));
      properties.put(ProducerConfig.ACKS_CONFIG, "1");
      properties.put(ProducerConfig.RETRIES_CONFIG, "10");
      properties.put(ProducerConfig.CLIENT_ID_CONFIG, MockarooProducer.get("CLIENT_ID_CONFIG"));
      properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
      properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
      properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, MockarooProducer.get("SCHEMA_REGISTRY_URL_CONFIG"));

      KafkaProducer<String, Dataset> kafkaProducer = new KafkaProducer<>(properties);

      String topic = (String) MockarooProducer.get("TOPIC");

      while (true) {
        MockarooData mockarooData = new MockarooData();

        String[] fields = ((String) mockarooData.get("mockaroo.fields")).split(",");

        for (Object o : mockarooData.getData()) {
          if (o instanceof JSONObject) {
            JSONObject json = (JSONObject) o;

            Dataset.Builder builder = Dataset.newBuilder();

            for (String field: fields) {
              Method method = builder.getClass().getMethod("set" + capitalizeFirstLetter(field), String.class);
              method.invoke(builder, json.get(field).toString());
            }

            Dataset dataset = builder.build();

            ProducerRecord<String, Dataset> producerRecord = new ProducerRecord<>(topic, dataset);

            try {
              kafkaProducer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                  if (e == null) {
                    logger.info("[KAFKA] Record " + json.toString() + " successfully submitted to: Partition " + recordMetadata.partition() + "; Offset: " + recordMetadata.offset());
                  } else {
                    logger.error("[KAFKA] The delivery has failed.", e);
                  }
                }
              });
            } catch (SerializationException e) {
              logger.error("[KAFKA] Error serializing Avro message.", e);
            }

            kafkaProducer.flush();

          }
        }
      }

      // kafkaProducer.close();

    } catch (IOException e) {
      logger.error("mockaroo.properties file does NOT exist!!!", e);
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    logger.info("[KAFKA] End.");
  }

  private static Object get(String property) {
    return properties.get(property);
  }

  private static String capitalizeFirstLetter(String original) {
    if (original == null || original.length() == 0) {
      return original;
    }
    return original.substring(0, 1).toUpperCase() + original.substring(1);
  }
}