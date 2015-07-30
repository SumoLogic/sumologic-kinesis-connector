package com.sumologic.client;

import java.io.IOException;

import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.sumologic.client.implementations.SumologicTransformer;
import com.sumologic.client.model.CloudWatchLogsMessageModel;
import com.sumologic.client.model.LogEvent;

import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A custom transfomer for {@link CloudWatchLogsMessageModel} records in JSON format. The output is in a format
 * usable for insertions to Sumologic.
 */
public class CloudWatchMessageModelSumologicTransformer 
  implements SumologicTransformer<CloudWatchLogsMessageModel> {
  private static final Logger LOG = Logger.getLogger(CloudWatchMessageModelSumologicTransformer.class.getName());
  
  private static CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
  
    /**
     * Creates a new KinesisMessageModelSumologicTransformer.
     */
    public CloudWatchMessageModelSumologicTransformer() {
        super();
    }

    @Override
    public String fromClass(CloudWatchLogsMessageModel message) {
      String jsonMessage = "";
      JSONObject outputObject;
      int recordsInMessageCount = 0;
     
      List<LogEvent> logEvents = message.getLogEvents();
      int logEventsSize = logEvents.size();
      for (int i=0;i<logEventsSize;i++) {
        LogEvent log = logEvents.get(i);
        
        outputObject = new JSONObject();
        try {
          // Header
          outputObject.put("logGroup", message.getLogGroup());
          outputObject.put("logStream", message.getLogStream());
          outputObject.put("messageType", message.getMessageType());
          outputObject.put("owner", message.getOwner());
          outputObject.put("subscriptionFilters", new JSONArray(message.getSubscriptionFilters()));

          // Body
          outputObject.put("id", log.getId());
          outputObject.put("message", log.getMessage());
          outputObject.put("timestamp", log.getTimestamp());
        } catch (JSONException e) {
          LOG.error("Unable to convert message into JSON String: "+e.getMessage());
        }
        jsonMessage += outputObject.toString();
        recordsInMessageCount++;
        if (i < logEventsSize - 1) {
          jsonMessage += '\n';
        }
      }
      return jsonMessage;
    }
    
    @Override
    public CloudWatchLogsMessageModel toClass(Record record) {
      byte[] decodedRecord = record.getData().array();
      String stringifiedRecord = SumologicKinesisUtils.decompressGzip(decodedRecord);
      
      if (stringifiedRecord == null) {
        LOG.error("Unable to decompress the record: "+new String(record.getData().array())
                 +"\nNot attempting to transform into a Message Model");
        return null;
      }
      
      ByteBuffer bufferedData = null;
      try {
        bufferedData = encoder.encode(CharBuffer.wrap(stringifiedRecord));
      } catch (CharacterCodingException e) {
        LOG.error("Unable to set the decompressed Record for serializing "+e.getMessage());
      }
      record.setData(bufferedData);

      try {
        return new ObjectMapper().readValue(
            record.getData().array(), 
            CloudWatchLogsMessageModel.class);
      } catch (IOException e) {
        LOG.error("Unable to convert the Record into a POJO: "+stringifiedRecord
                 +"\nerror: "+e.getMessage());
      } 
      return null;
    }
}
