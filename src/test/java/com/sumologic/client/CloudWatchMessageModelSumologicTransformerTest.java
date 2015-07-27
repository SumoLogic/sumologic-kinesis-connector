package com.sumologic.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Ignore;

import com.amazonaws.services.kinesis.model.Record;
import com.sumologic.client.CloudWatchMessageModelSumologicTransformer;
import com.sumologic.client.SimpleKinesisMessageModel;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;


public class CloudWatchMessageModelSumologicTransformerTest {
  public static Charset charset = Charset.forName("UTF-8");
  public static CharsetEncoder encoder = charset.newEncoder();
  
  @Test
  public void theTransformerShouldFailGracefullyWhenUnableToCompress () {
    CloudWatchMessageModelSumologicTransformer transfomer = new CloudWatchMessageModelSumologicTransformer();
    
    String randomData = "Some random string without GZIP compression";
    ByteBuffer bufferedData = null;
    try {
      bufferedData = encoder.encode(CharBuffer.wrap(randomData));
    } catch (Exception e) {
      Assert.fail("Getting error: "+e.getMessage());
    }
    
    Record mockedRecord = new Record();
    mockedRecord.setData(bufferedData);
    
    CloudWatchLogsMessageModel messageModel = transfomer.toClass(mockedRecord);
    
    
    Assert.assertNull(messageModel);
  }
  
  @Test
  public void theTransformerShouldSucceedWhenTransformingAProperJSON() {
    CloudWatchMessageModelSumologicTransformer transfomer = new CloudWatchMessageModelSumologicTransformer();
    
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
    
    byte[] compressData = SumologicSender.compressGzip(jsonData);
    
    ByteBuffer bufferedData = null;
    try {
      bufferedData = ByteBuffer.wrap(compressData);
    } catch (Exception e) {
      Assert.fail("Getting error: "+e.getMessage());
    }
    
    Record mockedRecord = new Record();
    mockedRecord.setData(bufferedData);
    
    CloudWatchLogsMessageModel messageModel = transfomer.toClass(mockedRecord);
    
    Assert.assertNotNull(messageModel);
  }
  
  @Test
  public void theTransformerShouldFailWhenTransformingAJSONWithTrailingCommas() {
    CloudWatchMessageModelSumologicTransformer transfomer = new CloudWatchMessageModelSumologicTransformer();
    
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
                    
    byte[] compressData = SumologicSender.compressGzip(jsonData);
    
    ByteBuffer bufferedData = null;
    try {
      bufferedData = ByteBuffer.wrap(compressData);
    } catch (Exception e) {
      Assert.fail("Getting error: "+e.getMessage());
    }
    
    Record mockedRecord = new Record();
    mockedRecord.setData(bufferedData);
    
    CloudWatchLogsMessageModel messageModel = null;
    messageModel = transfomer.toClass(mockedRecord);
    
    Assert.assertNull(messageModel);
  }
  
  @Test
  public void theTransfomerShouldSeparateBatchesOfLogs() {
    CloudWatchMessageModelSumologicTransformer transfomer = new CloudWatchMessageModelSumologicTransformer();
    
    String jsonData =  ""
                        +"{"
                        + "\"logEvents\": [{"
                        +                   "\"id\": \"3889492387492837492374982374897239847289374892\","
                        +                   "\"message\": \"1 23423532532 eni-ac9342k3492 10.1.1.75 66.175.209.17 123 123 17 1 76 1437755534 1437755549 ACCEPT OK\","
                        +                   "\"timestamp\": \"2342342342300\""
                        +                "},"
                        +                "{"
                        +                   "\"id\": \"3289429357928375892739857238975235235235\","
                        +                   "\"message\": \"1 23423516 eni-ac9342k3492 10.1.1.75 66.175.209.17 123 123 17 1 76 1437755534 1437755549 REJECT OK\","
                        +                   "\"timestamp\": \"2342352351616\""
                        +                "}],"
                        + "\"logGroup\": \"MyFirstVPC\","
                        + "\"logStream\": \"eni-ac6a7de4-all\","
                        + "\"messageType\": \"DATA_MESSAGE\","
                        + "\"owner\": \"2342352352\","
                        + "\"subscriptionFilters\": [\"MyFirstVPC\"]" 
                        + "}"
                      +"";
                    
    byte[] compressData = SumologicSender.compressGzip(jsonData);
    
    ByteBuffer bufferedData = null;
    try {
      bufferedData = ByteBuffer.wrap(compressData);
    } catch (Exception e) {
      Assert.fail("Getting error: "+e.getMessage());
    }
    
    Record mockedRecord = new Record();
    mockedRecord.setData(bufferedData);
    
    CloudWatchLogsMessageModel messageModel = null;
    messageModel = transfomer.toClass(mockedRecord);
    
    String debatchedMessage = transfomer.fromClass(messageModel);
    System.out.println(debatchedMessage);
    
    String[] messages = debatchedMessage.split("\n");
    Assert.assertTrue(messages.length == 2);
  }
}