package com.sumologic.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"logEvents",
"logGroup",
"logStream",
"messageType",
"owner",
"subscriptionFilters"
})

public class CloudWatchLogsMessageModel {
  
  @JsonProperty("logEvents")
  private List<LogEvent> logEvents = new ArrayList<LogEvent>();
  @JsonProperty("logGroup")
  private String logGroup;
  @JsonProperty("logStream")
  private String logStream;
  @JsonProperty("messageType")
  private String messageType;
  @JsonProperty("owner")
  private String owner;
  @JsonProperty("subscriptionFilters")
  private List<String> subscriptionFilters = new ArrayList<String>();
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();
  
  @JsonProperty("logEvents")
  public List<LogEvent> getLogEvents() {
    return logEvents;
  }

  @JsonProperty("logEvents")
  public void setLogEvents(List<LogEvent> logEvents) {
    this.logEvents = logEvents;
  }

  @JsonProperty("logGroup")
  public String getLogGroup() {
    return logGroup;
  }

  @JsonProperty("logGroup")
  public void setLogGroup(String logGroup) {
    this.logGroup = logGroup;
  }

  @JsonProperty("logStream")
  public String getLogStream() {
    return logStream;
  }

  @JsonProperty("logStream")
  public void setLogStream(String logStream) {
    this.logStream = logStream;
  }

  @JsonProperty("messageType")
  public String getMessageType() {
    return messageType;
  }

  @JsonProperty("messageType")
  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }
  
  @JsonProperty("owner")
  public String getOwner() {
    return owner;
  }

  @JsonProperty("owner")
  public void setOwner(String owner) {
    this.owner = owner;
  }

  @JsonProperty("subscriptionFilters")
  public List<String> getSubscriptionFilters() {
    return subscriptionFilters;
  }

  @JsonProperty("subscriptionFilters")
  public void setSubscriptionFilters(List<String> subscriptionFilters) {
    this.subscriptionFilters = subscriptionFilters;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}