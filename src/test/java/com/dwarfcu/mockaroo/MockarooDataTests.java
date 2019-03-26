package com.dwarfcu.mockaroo;

import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class MockarooDataTests {

  @Test
  public void getCSVDataset() {
    MockarooData mockarooData = new MockarooData();
    Assert.assertEquals(30, mockarooData.getDataString().length);
  }

  @Test
  public void getJSONDataset() {
    Properties properties = new Properties();
    properties.setProperty("mockaroo.format", "json");
    properties.setProperty("mockaroo.url", "https://api.mockaroo.com/api/9b0d87e0?count=1000&key=9ae0b0d0");

    MockarooData mockarooData = new MockarooData(properties);
    Assert.assertEquals(1000, mockarooData.getDataJson().length());
  }
}