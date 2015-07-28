package com.sumologic.client;

import org.junit.Assert;
import org.junit.Test;

public class SumologicKinesisUtilsTest {
  @Test
  public void compressDecompressGzipTest() {
    String data = "a string of characters";
    
    byte[] compressData = SumologicKinesisUtils.compressGzip(data);
    String result = SumologicKinesisUtils.decompressGzip(compressData);
    
    Assert.assertTrue(data.equals(result));
  }
  
  @Test
  public void properJSONVerificationShouldReturnTrue() {
    String jsonData =  ""
        +"{"
        + "\"logEvents\": [{"
        +                   "\"id\": \"3889492387492837492374982374897239847289374892\","
        +                   "\"message\": \"1 23423532532 eni-ac9342k3492 10.1.1.75 66.175.209.17 123 123 17 1 76 1437755534 1437755549 ACCEPT OK\","
        +                   "\"timestamp\": \"2342342342300\""
        +                "}],"
        + "\"logGroup\": \"MyFirstVPC\","
        + "\"logStream\": \"eni-ac6a7de4-all\","
        + "\"messageType\": \"DATA_MESSAGE\","
        + "\"owner\": \"2342352352\","
        + "\"subscriptionFilters\": [\"MyFirstVPC\"]" 
        + "}"
      +"";
    
    Assert.assertTrue(SumologicKinesisUtils.verifyJSON(jsonData));
  }
  
  @Test
  public void malformedJSONVerificationShouldReturnTrue() {
    String jsonData =  ""
        +"{"
        + "\"logEvents\": [{"
        +                   "\"id\": \"3889492387492837492374982374897239847289374892\","
        +                   "\"message\": \"1 23423532532 eni-ac9342k3492 10.1.1.75 66.175.209.17 123 123 17 1 76 1437755534 1437755549 ACCEPT OK\","
        +                   "\"timestamp\": \"2342342342300\""
        +                "}],"
        + "\"logGroup\": \"MyFirstVPC\","
        + "\"logStream\": \"eni-ac6a7de4-all\","
        + "\"messageType\": \"DATA_MESSAGE\","
        + "\"owner\": \"2342352352\","
        + "\"subscriptionFilters\": [\"MyFirstVPC\"]," 
        + "}"
      +"";
    
    Assert.assertFalse(SumologicKinesisUtils.verifyJSON(jsonData));
  }
}