package com.sumologic.client.implementations;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Ignore;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sumologic.client.implementations.SumologicEmitter;

public class SumologicEmitterTest {
  
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8089);
  
  private static final String MOCKED_HOST = "http://localhost:8089";
  private static final String MOCKED_COLLECTION = "/sumologic/collections/1234";

  @Before
  public void setUp() {
    mockEmitMessages();
  }

  @Test
  public void theEmitterShouldReturnTheListParameterWhenFailing () {
    String url = MOCKED_HOST + "/sumologic/collections/fake-url";
    
    List<String> messages = new ArrayList<String>();
    messages.add("This is message #1");
    messages.add("This is message #2");
    messages.add("This is message #3");
    messages.add("This is message #4");
    
    SumologicEmitter emitter = new SumologicEmitter(url);
    List <String> notEmittedMessages = emitter.sendBatchConcatenating(messages);
    
    Assert.assertEquals(messages, notEmittedMessages);
  }
  
  @Test
  public void theEmitterShouldReturnAnEmptyListOnSuccess () {
    String url = MOCKED_HOST + MOCKED_COLLECTION;
    
    List<String> messages = new ArrayList<String>();
    messages.add("This is message #1");
    messages.add("This is message #2");
    messages.add("This is message #3");
    messages.add("This is message #4");
    
    SumologicEmitter emitter = new SumologicEmitter(url);
    List <String> notEmittedMessages = emitter.sendBatchConcatenating(messages);
    
    Assert.assertEquals(0, notEmittedMessages.size());
  }
  
  private void mockEmitMessages () {
    WireMock.stubFor(WireMock.post(WireMock.urlMatching(MOCKED_COLLECTION))
          .willReturn(WireMock.aResponse()
          .withStatus(200)
          .withHeader("Content-Type", "text/html")
          .withBody("")));
  }
  
}