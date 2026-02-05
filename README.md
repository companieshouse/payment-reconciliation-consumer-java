# Payment Reconciliation Consumer

## Overview
The Payment Reconciliation Consumer is designed to process payment-related messages from a Kafka topic. It ensures that payment events are reconciled and stored in the database while adhering to security and business rules. This document provides a detailed design of the service, focusing on its architecture, components, and business logic.

**Note:** All monetary amounts are converted from pence (integer) to pounds (decimal) before being stored or processed, to ensure consistency and clarity in financial records.

## Requirements
In order to run the service locally you will need the following:
- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)

## Getting started
To checkout and build the service:
1. Clone [Docker CHS Development](https://github.com/companieshouse/docker-chs-development) and follow the steps in the README.
2. Run ./bin/chs-dev services enable payment-reconciliation-consumer-java kafka3 zookeeper-kafka3
3. Run ./bin/chs-dev development enable Payment Reconciliation Consumer-java if you wish to see changes in the code
4. TODO - add how to use the [tool](https://github.com/companieshouse/chs-tools/tree/add_kafka_message_sender)

These instructions are for a local docker environment.

## Configuration
For detailed configuration variables, refer to the [design document](./docs/design/DESIGN.md).

## Error Handling
For detailed error handling strategies, refer to the [design document](./docs/design/DESIGN.md).

## Design
[Design Document](./docs/design/DESIGN.md)

## Testing
[Testing](./docs/testing/readme.md)
