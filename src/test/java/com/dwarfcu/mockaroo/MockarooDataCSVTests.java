package com.dwarfcu.mockaroo;

import org.junit.Assert;
import org.junit.Test;

public class MockarooDataCSVTests {

  private MockarooData mockarooData = new MockarooData();

  @Test
  public void getCSVDataset() {
    Assert.assertEquals(30, mockarooData.getDataString().length);
  }

}