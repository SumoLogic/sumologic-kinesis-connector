package com.sumologic.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.Response;

public class SumologicSender {
  private static final Log LOG = LogFactory.getLog(SumologicSender.class);

  private String url = null;  
  private AsyncHttpClient client = null;

  private static final int RETRIES = 3;
  private static final int SLEEP_TIME = 1000;
	    
	public SumologicSender(String url) {
		  this.url = url;

	    Builder builder = new AsyncHttpClientConfig.Builder();
	    this.client = new AsyncHttpClient(builder.build());
	}
	
  private BoundRequestBuilder clientPreparePost(String url){
    if (this.client.isClosed()){
      Builder builder = new AsyncHttpClientConfig.Builder();
      this.client = new AsyncHttpClient(builder.build()); 
    }
    return this.client.preparePost(url);
  }

	public boolean sendToSumologic(String data) throws IOException{
	  int retries = RETRIES;
	  int sleep_time = SLEEP_TIME;
	  int statusCode = -1;
	  
	  do {
	    BoundRequestBuilder builder = null;
	    builder = this.clientPreparePost(url);
	    
	    byte[] compressedData = SumologicKinesisUtils.compressGzip(data);
	    
	    builder.setHeader("Content-Encoding", "gzip");
	    builder.setBody(compressedData);
	    
	    Response response = null;
      try {
        response = builder.execute().get();
        statusCode = response.getStatusCode();
      } catch (InterruptedException e) {
        LOG.error("Can't send POST to Sumologic "+e.getMessage());
      } catch (ExecutionException e) {
        LOG.error("Can't send POST to Sumologic "+e.getMessage());
      }
      
      if (statusCode == 429) {
        LOG.warn("Got TOO MANY REQUESTS from Sumologic");
        retries--;
        try {
          Thread.sleep(sleep_time);
        } catch (InterruptedException ignore) {}
      }
	  } while (statusCode == 429 && retries > 0);
    
    // Check if the request was successful;
    if (statusCode != 200) {
      LOG.warn(String.format("Received HTTP error from Sumo Service: %d", statusCode));
      return false;
    }
    else{ 
      return true;
    } 
	}
}