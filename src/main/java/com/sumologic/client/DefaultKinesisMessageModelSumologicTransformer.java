package com.sumologic.client;

import java.io.IOException;

import com.amazonaws.services.kinesis.model.Record;
import com.sumologic.client.implementations.SumologicTransformer;
import com.sumologic.client.model.SimpleKinesisMessageModel;

/**
 * A custom transfomer for {@link SimpleKinesisMessageModel} records in JSON format. The output is in a format
 * usable for insertions to Sumologic.
 */
public class DefaultKinesisMessageModelSumologicTransformer implements
        SumologicTransformer<SimpleKinesisMessageModel> {
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
