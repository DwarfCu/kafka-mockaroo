package com.dwarfcu.kafka.mockaroo;

import com.dwarfcu.kafka.Dataset;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
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

      logger.debug("[kafka.properties]:" + properties.toString());

      KafkaProducer<String, GenericRecord> kafkaProducer = new KafkaProducer<>(properties);

      String topic = (String) MockarooProducer.get("TOPIC");

      //Create schema
      String avroSchema = MockarooProducer.readResourcesAsString("/avro/avro-schema.avsc");
      logger.info("[Avro Schema]" + avroSchema);

      Schema.Parser parser = new Schema.Parser();
      Schema schema = parser.parse(avroSchema);

      while (true) {
        MockarooData mockarooData = new MockarooData();

        for (Object o : mockarooData.getData()) {
          if (o instanceof JSONObject) {
            JSONObject json = (JSONObject) o;
            logger.debug("[JSON] Input:" + json.toString());

            // Convert JSON to Avro according to Avro Schema
            InputStream inputStreamJson = new ByteArrayInputStream(json.toString().getBytes());
            DataInputStream dataInputStreamJson = new DataInputStream(inputStreamJson);

            try {
              Decoder decoder = DecoderFactory.get().jsonDecoder(schema, dataInputStreamJson);

              DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
              GenericRecord dataset = reader.read(null, decoder);

              // Create Kafka record
              ProducerRecord<String, GenericRecord> producerRecord = new ProducerRecord<>(topic, dataset);

              // Send record to topic
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

                kafkaProducer.flush();

              } catch (SerializationException e) {
                logger.error("[KAFKA] Error serializing Avro message.", e);
              }

            } catch (AvroTypeException e) {
              logger.warn("[Avro-SchemaRegistry] " + json, e);
            } catch (IOException e) {
              logger.error(e);
            }
          }
        }
      }

      // kafkaProducer.close();

    } catch (IOException e) {
      logger.error("mockaroo.properties file does NOT exist!!!", e);
    } catch (Exception e) {
      logger.error(e);
    }
    logger.info("[KAFKA] End.");
  }

  private static Object get(String property) {
    return properties.get(property);
  }

  private static String readResourcesAsString (String filename) throws Exception {
    InputStream inputStream = MockarooProducer.class.getResourceAsStream(filename);

    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

    return org.apache.commons.io.IOUtils.toString(reader);
  }
}