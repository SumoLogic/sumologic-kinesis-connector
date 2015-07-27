package com.sumologic.client.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sumologic.client.SumologicSender;
import com.sumologic.client.KinesisConnectorForSumologicConfiguration;

import com.amazonaws.services.kinesis.connectors.KinesisConnectorConfiguration;
import com.amazonaws.services.kinesis.connectors.UnmodifiableBuffer;
import com.amazonaws.services.kinesis.connectors.interfaces.IEmitter;

/**
 * This class is used to store records from a stream to Sumologic log files. It requires the use of a
 * SumologicTransformer, which is able to transform records into a format that can be sent to
 * Sumologic.
 */
public class SumologicEmitter implements IEmitter<String> {
    private static final Log LOG = LogFactory.getLog(SumologicEmitter.class);

    private SumologicSender sender;
    private KinesisConnectorForSumologicConfiguration config;
    private static final boolean SEND_RECORDS_IN_BATCHES = true;

    public SumologicEmitter(KinesisConnectorConfiguration configuration) {
        this.config = (KinesisConnectorForSumologicConfiguration) configuration;
        sender = new SumologicSender(this.config.SUMOLOGIC_URL);
    }
    
    public SumologicEmitter(String url) {
        sender = new SumologicSender(url);
    }

    @Override
    public List<String> emit(final UnmodifiableBuffer<String> buffer)
        throws IOException {
        List<String> records = buffer.getRecords();
        if (SEND_RECORDS_IN_BATCHES) {
          return sendBatchConcatenating(records);
        } else {
          return sendRecordsOneByOne(records);
        }
    }
    
    public List<String> sendBatchConcatenating(List<String> records) {
      boolean success = false; 
      
      String message = "";
      for(String record: records) {
        message += record;
        message += "\n";
      }
      try {
        LOG.info("Sending batch of: "+records.size()+" records");
        success = sender.sendToSumologic(message);
      } catch (IOException e) {
        LOG.warn("Couldn't send record to Sumologic: "+e.getMessage());
        return records;
      }
      
      if (success)
        return new ArrayList<String>();
      else {
        return records;
      }
    }
    
    public List<String> sendRecordsOneByOne (List<String> records) {
      ArrayList<String> failedRecords = new ArrayList<String>();
      for (String record: records) {
        try {
          if (!sender.sendToSumologic(record)) {
            failedRecords.add(record);
          }
        } catch (IOException e) {
          LOG.warn("Couldn't send record: "+record);
        }
      }
      LOG.info("Sent records: "+(records.size()-failedRecords.size())+" failed: "+failedRecords.size());
      return failedRecords;
    }

    @Override
    public void fail(List<String> records) {
        for (String record : records) {
            LOG.error("Could not emit record: " + record);
        }
    }

    @Override
    public void shutdown() {
    }
}