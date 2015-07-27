package com.sumologic.client;

import java.io.IOException;

import com.amazonaws.services.kinesis.connectors.BasicJsonTransformer;
import com.amazonaws.services.kinesis.model.Record;
import com.sumologic.client.SimpleKinesisMessageModel;
import com.sumologic.client.implementations.SumologicEmitter;
import com.sumologic.client.implementations.SumologicTransformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;


/**
 * A custom transfomer for {@link SimpleKinesisMessageModel} records in JSON format. The output is in a format
 * usable for insertions to Sumologic.
 */
public class DefaultKinesisMessageModelSumologicTransformer implements
        SumologicTransformer<SimpleKinesisMessageModel> {

  private static final Log LOG = LogFactory.getLog(DefaultKinesisMessageModelSumologicTransformer.class);
  
    /**
     * Creates a new KinesisMessageModelSumologicTransformer.
     */
    public DefaultKinesisMessageModelSumologicTransformer() {
        super();
    }

    @Override
    public String fromClass(SimpleKinesisMessageModel message) {
        return message.toString();
    }

    @Override
    public SimpleKinesisMessageModel toClass(Record record) throws IOException {
      byte[] decodedRecord = record.getData().array();
      String stringifiedRecord = new String(decodedRecord);

      return new SimpleKinesisMessageModel(stringifiedRecord);
    }
}
