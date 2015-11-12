# CloudFormation templates for Kinesis-Sumologic Connector

This folder contains the Cloudformation templates to setup a **Kinesis-Sumologic Connector** for Amazon VPC Flow Logs.  

## Overview

These templates are included:
 + **cwl_kinesis.template**: This template will create a new VPC for the new EC2 instance that hosts the connector. 
 + **cwl_kinesis_custom_vpc.template**: This template will use an existing VPC and security group for the new EC2 instance that hosts the connector. 

In any scenario, the template will create a new Kinesis stream and subscribes the user specified log group to this stream.

## Special Notes
+ It is strongly recommended to use a separate log group for each VPC to differentiate their logs. 
+ By default, each template (and hence the corresponding stack) runs under the region the AWS user uses at run time. You will need to creat one separate stack per region. Within the same region, when there are multiple CloudWatch log groups, you can create multiple stacks, or create one stack and use the created Kinesis stream by this stack for multiple log groups. When use multiple stacks in the *same* region, make sure to use different values for the parameter "KinesisConnectorAppName" of the template. 




