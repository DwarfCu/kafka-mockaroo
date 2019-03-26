package com.dwarfcu.mockaroo;

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
  private JSONArray datasetJson;
  private String[] datasetString;

  public MockarooData() {
    logger.info("[Mockaroo] Getting dataset...");

    try {
      properties = new Properties();
      properties.load(MockarooData.class.getClassLoader().getResource("mockaroo.properties").openStream());
    } catch (IOException e) {
      logger.error("mockaroo.properties file does NOT exist!!!", e.getMessage());
    }

    downloadMockarooData();
  }

  public MockarooData(Properties properties) {
    if (properties.containsKey("mockaroo.format") && (properties.containsKey("mockaroo.url"))){
      this.properties = properties;

      downloadMockarooData();
    } else {
      logger.error("[Mockaroo] Some properties (mockaroo.format or mockaroo.url) are missing.");
      System.exit(0);
    }
  }

  private void downloadMockarooData() {
    try {
      URL url = new URL((String) MockarooData.get("mockaroo.url"));

      String format = (String) MockarooData.get("mockaroo.format");

      if (format.toLowerCase().equals("json")) {
        datasetJson = new JSONArray(IOUtils.toString(url, Charset.forName("UTF-8")));
      } else if (format.toLowerCase().equals("csv")) {
        datasetString = IOUtils.toString(url, Charset.forName("UTF-8")).split("\n");
      } else {
        logger.error("[Mockaroo] Unknow format: " + format + ". Please, check mockaroo.properties.");
        System.exit(0);
      }

      logger.info("[Mockaroo] Dataset downloaded.");
    } catch (MalformedURLException e) {
      logger.error("mockaroo.url malformed!!!", e.getMessage());
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }


  private static Object get(String property) {
    return properties.get(property);
  }

  public JSONArray getDataJson() {
    return datasetJson;
  }

  public String[] getDataString() {
    return datasetString;
  }
}