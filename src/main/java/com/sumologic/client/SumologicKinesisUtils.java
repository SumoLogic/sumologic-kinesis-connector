package com.sumologic.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SumologicKinesisUtils {
  private static final Logger LOG = Logger.getLogger(SumologicKinesisUtils.class.getName());

  public static byte[] compressGzip(String data) {
    if (data == null || data.length() == 0) {
      return null;
    }
    
    ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
    GZIPOutputStream gzip;
    try {
      gzip = new GZIPOutputStream(outputStream);
    } catch (IOException e) {
      LOG.error("Cannot compress into GZIP "+e.getMessage());
      return null;
    }
    
    // Put data into the GZIP buffer
    try {
      gzip.write(data.getBytes("UTF-8"));
      gzip.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  
    return outputStream.toByteArray();
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