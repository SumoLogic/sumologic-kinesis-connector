package com.sumologic.client;

import java.io.IOException;

import com.amazonaws.services.kinesis.connectors.BasicJsonTransformer;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.sumologic.client.SimpleKinesisMessageModel;
import com.sumologic.client.implementations.SumologicTransformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

/**
 * A custom transfomer for {@link CloudWatchLogsMessageModel} records in JSON format. The output is in a format
 * usable for insertions to Sumologic.
 */
public class CloudWatchMessageModelSumologicTransformer 
  implements SumologicTransformer<CloudWatchLogsMessageModel> {

  private static final Log LOG = LogFactory.getLog(CloudWatchMessageModelSumologicTransformer.class);

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
        if (i < logEventsSize - 1) {
          jsonMessage += '\n';
        }
      }
      return jsonMessage;
    }
    
    @Override
    public CloudWatchLogsMessageModel toClass(Record record) {
      byte[] decodedRecord = record.getData().array();
      String stringifiedRecord = decompressGzip(decodedRecord);
      
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

    public static String decompressGzip(byte[] compressedData) {
      try {
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressedData));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        
        String outStr = "";
        String line;
        while ((line=bf.readLine())!=null) {
          outStr += line;
        }
        return outStr;
      } catch (IOException exc) {
        LOG.warn("Exception during decompression of data: " + exc.getMessage());
        return null;
      }
    }
    
    public static String byteBufferToString(ByteBuffer buffer){
      String data = "";
      CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
      try{
        int old_position = buffer.position();
        data = decoder.decode(buffer).toString();
        buffer.position(old_position);  
      }catch (Exception e){
        e.printStackTrace();
        return "";
      }
      return data;
    }

    private static final Gson gson = new Gson();
    public static boolean verifyJSON(String json) {
      try {
          gson.fromJson(json, Object.class);
          return true;
      } catch(com.google.gson.JsonSyntaxException ex) { 
          return false;
      }
    }

}
