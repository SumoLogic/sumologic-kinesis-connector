package com.sumologic.kinesis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.sumologic.client.KinesisConnectorForSumologicConfiguration;
import com.sumologic.kinesis.KinesisConnectorExecutorBase;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.kinesis.connectors.KinesisConnectorConfiguration;

/**
 * This class defines the execution of a Amazon Kinesis Connector.
 * 
 */
public abstract class KinesisConnectorExecutor<T, U> extends KinesisConnectorExecutorBase<T, U> {
    private static final Logger LOG = Logger.getLogger(KinesisConnectorExecutor.class.getName());

    // Create Stream Source constants
    private static final String CREATE_STREAM_SOURCE = "createStreamSource";
    private static final String LOOP_OVER_STREAM_SOURCE = "loopOverStreamSource";
    private static final String INPUT_STREAM_FILE = "inputStreamFile";
    
    private static final boolean DEFAULT_CREATE_STREAM_SOURCE = false;
    private static final boolean DEFAULT_LOOP_OVER_STREAM_SOURCE = false;


    // Class variables
    protected final KinesisConnectorForSumologicConfiguration config;
    private final Properties properties;

    /**
     * Create a new KinesisConnectorExecutor based on the provided configuration (*.propertes) file.
     * 
     * @param configFile
     *        The name of the configuration file to look for on the classpath
     */
    public KinesisConnectorExecutor(String configFile) {
        // Load configuration properties
        InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);

        if (configStream == null) {
            String msg = "Could not find resource " + configFile + " in the classpath";
            throw new IllegalStateException(msg);
        }
        properties = new Properties();
        try {
            properties.load(configStream);
            configStream.close();
        } catch (IOException e) {
            String msg = "Could not load properties file " + configFile + " from classpath";
            throw new IllegalStateException(msg, e);
        }
        this.config = new KinesisConnectorForSumologicConfiguration(properties, getAWSCredentialsProvider());

        // Send sample data to AWS Kinesis if specified in the properties file
        setupInputStream();

        // Initialize executor with configurations
        super.initialize((KinesisConnectorConfiguration)config);
    }

    /**
     * Returns an {@link AWSCredentialsProvider} with the permissions necessary to accomplish all specified
     * tasks. At the minimum it will require read permissions for Amazon Kinesis. Additional read permissions
     * and write permissions may be required based on the Pipeline used.
     * 
     * @return
     */
    public AWSCredentialsProvider getAWSCredentialsProvider() {
        return new ClasspathPropertiesFileCredentialsProvider("SumologicConnector.properties");
    }

    /**
     * Helper method to spawn the {@link StreamSource} in a separate thread.
     */
    private void setupInputStream() {
        if (parseBoolean(CREATE_STREAM_SOURCE, DEFAULT_CREATE_STREAM_SOURCE, properties)) {
            String inputFile = properties.getProperty(INPUT_STREAM_FILE);
            StreamSource streamSource;
            if (config.BATCH_RECORDS_IN_PUT_REQUEST) {
                streamSource =
                        new BatchedStreamSource(config, inputFile, parseBoolean(LOOP_OVER_STREAM_SOURCE,
                                DEFAULT_LOOP_OVER_STREAM_SOURCE,
                                properties));

            } else {
                streamSource =
                        new StreamSource(config, inputFile, parseBoolean(LOOP_OVER_STREAM_SOURCE,
                                DEFAULT_LOOP_OVER_STREAM_SOURCE,
                                properties));
            }
            Thread streamSourceThread = new Thread(streamSource);
            LOG.info("Starting stream source.");
            streamSourceThread.start();
        }
    }


    /**
     * Helper method used to parse boolean properties.
     * 
     * @param property
     *        The String key for the property
     * @param defaultValue
     *        The default value for the boolean property
     * @param properties
     *        The properties file to get property from
     * @return property from property file, or if it is not specified, the default value
     */
    private static boolean parseBoolean(String property, boolean defaultValue, Properties properties) {
        return Boolean.parseBoolean(properties.getProperty(property, Boolean.toString(defaultValue)));
    }
}