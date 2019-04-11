# Kinesis-Sumologic Connector

The **Kinesis-Sumologic Connector** is a Java connector that acts as a pipeline between an [Amazon Kinesis] stream and a [Sumologic] Collection. Data gets fetched from the Kinesis Stream, transformed into a POJO and then sent to the Sumologic Collection as JSON. End-user setup instructions can be found in [here](https://support.sumologic.com/hc/en-us/articles/209667668-Sumo-Logic-App-for-Amazon-VPC-Flow-Logs-using-Kinesis).

## Requirements

 + **Java JDK 1.8**: This connector has been built with Java version 1.8.
 + **Maven**: A pom.xml file has been provided to build the connector with Maven.
 + **AWS Kinesis Account**: An Amazon AWS Kinesis account to use as a source of data.
 + **Sumologic Account**: A Sumologic account to use as a destination.

## Overview

Incoming records from one (or many) Shards of an AWS Kinesis Stream will be read using the [Kinesis Client Library]. Records will be:

 + **Transformed**: Raw records will be transformed into a POJO using a Kinesis Model class and then serialized. The transformer used will be specified in the properties file.
 + **Filtered**: A filter may be applied to the records. Default filter will let all records pass.
 + **Buffered**: A custom buffer may be used to define thresholds that, when crossed, will flush all records into the emitter.
 + **Emitted**: The records will get send to the Sumologic Collector.

## Installation

The appender can be added to your project using Maven Central by adding the following dependency to a POM file:

```
<dependency>
  <groupId>com.sumologic</groupId>
  <artifactId>kinesis-sumologic-connector</artifactId>
  <version>0.2</version>
</dependency>
```

## Configuration

A sample properties file is provided, which should be modified to use your AWS Accounts (**accessKey** and **secretKey**), Kinesis Stream(**kinesisInputStream**), Sumologic HTTP source (**sumologicUrl**), App Name (**appName**) and Transformer class used (**transformerClass**). Reading from multiple kinesis streams is also supported (see PR14) by specifying multiple config files, launching multiple SumologicExecutors like so:
```
mvn clean compile exec:java -Dexec.args="app1.properties app2.properties"
```
The SumologicConnector.properties file is still required to be present in the working directory, as it's hardcoded into the application as the file AWS credentials are read from. If no .properties files are passed as arguments, SumologicConnector.properties is assumed as the only SumologicExecutor

## Running the Connector with Maven

After modifying the .properties file, run the connector using Maven
```
mvn clean compile exec:java -Dargs="SumologicConnector.properties"
```

## Building the JAR
After modifying the .properties file, build the JAR file using Maven
```
mvn clean install
```

## Running the JAR
```
java -jar target/kinesis-sumologic-connector-0.2.jar SumologicConnector.properties
```


## Related sources

[Amazon Kinesis](http://aws.amazon.com/kinesis/)

[Sumologic](https://www.sumologic.com/)

[Open JDK 8](http://openjdk.java.net/projects/jdk8)

[Maven](http://maven.apache.org/)

[AWS Kinesis Account](http://aws.amazon.com/account/)

[Sumologic Account](https://www.sumologic.com/pricing/)

[Kinesis Client Library](https://github.com/awslabs/amazon-kinesis-client/)
