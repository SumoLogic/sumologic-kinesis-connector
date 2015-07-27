package com.sumologic.client.implementations;

import com.amazonaws.services.kinesis.connectors.interfaces.ITransformer;

/**
 * This interface defines an ITransformer that can transform an object of any type if necesary.
 * 
 * @param <T>
 */
public interface SumologicTransformer<T> extends ITransformer<T, String> {

}
