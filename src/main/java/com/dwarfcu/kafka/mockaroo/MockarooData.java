package com.dwarfcu.kafka.mockaroo;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

public class MockarooData {

  private static final Logger logger = LogManager.getLogger(MockarooData.class.getName());

  private static Properties properties;
  private JSONArray dataset;

  public MockarooData() {
    logger.info("[Mockaroo] Getting dataset...");

    try {
      properties = new Properties();
      properties.load(MockarooData.class.getClassLoader().getResource("mockaroo.properties").openStream());

      URL url = new URL((String) this.get("mockaroo.url"));

      dataset = new JSONArray(IOUtils.toString(url, Charset.forName("UTF-8")));

      logger.info("[Mockaroo] Dataset downloaded.");
    } catch (MalformedURLException e) {
      logger.error("mockaroo.url malformed!!!", e.getMessage());
    } catch (IOException e) {
      logger.error("mockaroo.properties file does NOT exist!!!", e.getMessage());
    }
  }

  public Object get(String property) {
    return properties.get(property);
  }

  public JSONArray getData() {
    return dataset;
  }
}