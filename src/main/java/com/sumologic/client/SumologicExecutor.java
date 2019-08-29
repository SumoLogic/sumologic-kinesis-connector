package com.sumologic.client;

import java.util.ArrayList;

import com.sumologic.kinesis.KinesisConnectorRecordProcessorFactory;
import com.sumologic.kinesis.KinesisConnectorExecutor;
import com.sumologic.client.SumologicMessageModelPipeline;
import com.sumologic.client.model.SimpleKinesisMessageModel;

public class SumologicExecutor extends
		KinesisConnectorExecutor<SimpleKinesisMessageModel, String> {
	private static String defaultConfigFile = "SumologicConnector.properties";

	public SumologicExecutor() {
		super();
	}

	/**
	 * SumologicExecutor constructor.
	 * 
	 * @param configFile
	 *            Properties for the connector
	 */
	public SumologicExecutor(String configFile) {
		super(configFile);
	}

	@Override
	public KinesisConnectorRecordProcessorFactory<SimpleKinesisMessageModel, String> getKinesisConnectorRecordProcessorFactory() {
		return new KinesisConnectorRecordProcessorFactory<SimpleKinesisMessageModel, String>(
				new SumologicMessageModelPipeline(), config);
	}

	/**
	 * Main method starts and runs the SumologicExecutor.
	 * 
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		ArrayList<String> configFiles = new ArrayList<String>();
		ArrayList<Thread> executorThreads = new ArrayList<Thread>();

		Boolean use_env = false;

		for (String arg : args) {
			if (arg.endsWith(".properties")) {
				configFiles.add(arg);
			} else if (arg.equals("use-env")) {
				use_env = true;
			}
		}

		if (use_env) {
			KinesisConnectorExecutor<SimpleKinesisMessageModel, String> sumologicExecutor = new SumologicExecutor();

			Thread executorThread = new Thread(sumologicExecutor);
			executorThreads.add(executorThread);
			executorThread.start();

		} else {
			// if none of the arguments contained a config file, try the default
			// file name
			if (configFiles.size() == 0) {
				configFiles.add(defaultConfigFile);
			}

			for (String configFile : configFiles) {
				KinesisConnectorExecutor<SimpleKinesisMessageModel, String> sumologicExecutor = new SumologicExecutor(
						configFile);

				Thread executorThread = new Thread(sumologicExecutor);
				executorThreads.add(executorThread);
				executorThread.start();
			}
		}

		for (Thread executorThread : executorThreads) {
			executorThread.join();
		}
	}
}
