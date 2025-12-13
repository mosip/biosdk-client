[![Maven Package upon a push](https://github.com/mosip/biosdk-client/actions/workflows/push_trigger.yml/badge.svg?branch=develop)](https://github.com/mosip/biosdk-client/actions/workflows/push_trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?branch=develop&project=mosip_biosdk-client&metric=alert_status)](https://sonarcloud.io/dashboard?branch=develop&id=mosip_biosdk-client)

# Bio-SDK Client

## Overview

The **Bio-SDK Client** library provides a standardized implementation of [IBioApiV2](https://github.com/mosip/bio-utils/blob/master/kernel-biometrics-api/src/main/java/io/mosip/kernel/biometrics/spi/IBioApiV2.java) interface that:

- Connects with Bio-SDK services for biometric processing functionality.
- Enables ID Authentication and ID Repository services to perform 1:N matching, segmentation, and extraction.
- Provides a unified API for biometric operations across MOSIP services.
- Supports integration with various biometric SDK providers.
- Handles biometric quality checks and validation.

For a complete functional overview and capabilities, refer to the **[official documentation](https://docs.mosip.io/1.2.0/biometrics/biometric-sdk)**.

## Features

- Implementation of IBioApiV2 interface
- 1:N biometric matching
- Biometric segmentation and extraction
- Quality assessment and validation
- Multi-modal biometric support (fingerprint, iris, face)
- Integration with external Bio-SDK services
- Retry and fault-tolerance mechanisms
- Caching for improved performance

## Services

The Bio-SDK Client module is used as a library dependency by:

1. **ID Authentication** - For biometric-based authentication
2. **ID Repository** - For biometric data management and storage
3. **Registration Processor** - For biometric validation and deduplication

## Local Setup

The project can be set up for development or contribution:

### Prerequisites

Before you begin, ensure you have the following installed:

- **JDK**: 21.0.3
- **Maven**: 3.9.6
- **Docker**: Latest stable version (for running Bio-SDK services)

### Runtime Dependencies

- `kernel-biometrics-api.jar` - Core biometrics API interfaces
- `kernel-auth-adapter.jar` - IAM authentication adapter

### Configuration

Bio-SDK Client uses the following configuration files that are accessible in this [repository](https://github.com/mosip/mosip-config).
Please refer to the required released tagged version for configuration:
- [application-default.properties](https://github.com/mosip/mosip-config/blob/master/application-default.properties)
- [id-authentication-default.properties](https://github.com/mosip/mosip-config/blob/master/id-authentication-default.properties)

For detailed configuration options, refer to the [Configuration Guide](docs/configuration.md).

## Installation

### Local Setup (for Development or Contribution)

1. Make sure the Bio-SDK service is running and accessible.

2. Clone the repository:

```text
git clone https://github.com/mosip/biosdk-client.git
cd biosdk-client
```

3. Build the project:

```text
mvn clean install -Dgpg.skip=true
```

4. For library usage:
   - Include as a Maven dependency in your project
   - The library is used by ID Authentication, ID Repository, and Registration Processor

5. Example Maven dependency:

```xml
<dependency>
    <groupId>io.mosip.biosdk</groupId>
    <artifactId>biosdk-client</artifactId>
    <version>${biosdk.client.version}</version>
</dependency>
```

### Local Setup with Docker

For testing with Bio-SDK services:

1. Start the Bio-SDK service container:

```text
docker run -d -p 9099:9099 \
  --name biosdk-service \
  mosipid/biosdk-service:latest
```

2. Verify the service is running:

```text
curl http://localhost:9099/health
```

## Deployment

### Kubernetes

To deploy services using Bio-SDK Client on a Kubernetes cluster, refer to the [Sandbox Deployment Guide](https://docs.mosip.io/1.2.0/deploymentnew/v3-installation).

Verify deployment:

```text
kubectl get pods -n <namespace>
kubectl logs -n <namespace> <pod-name>
```

## Upgrade

### Upgrade Steps

1. Backup your current configuration from config server
2. Stop the dependent services (ID Authentication, ID Repository)
3. Update the biosdk-client version in your project's POM file
4. Rebuild and redeploy dependent services
5. Verify logs and health endpoints
6. Run validation tests

For major version upgrades, refer to the [MOSIP Release Notes](https://docs.mosip.io/1.2.0/releases) for breaking changes.

## Documentation

For more detailed documentation, check the [docs](docs) directory.

### API Documentation

API interfaces and usage details:

- **IBioApiV2 Interface**: [GitHub Source](https://github.com/mosip/bio-utils/blob/master/kernel-biometrics-api/src/main/java/io/mosip/kernel/biometrics/spi/IBioApiV2.java)
- **GitHub Pages**: [MOSIP API Documentation](https://mosip.github.io/documentation/)

### Product Documentation

To learn more about Bio-SDK Client from a functional perspective and use case scenarios, refer to our main documentation: [Biometric SDK](https://docs.mosip.io/1.2.0/biometrics/biometric-sdk).

## Contribution & Community

• To learn how you can contribute code to this application, [click here](https://docs.mosip.io/1.2.0/community/code-contributions).

• If you have questions or encounter issues, visit the [MOSIP Community](https://community.mosip.io/) for support.

• For any GitHub issues: [Report here](https://github.com/mosip/biosdk-client/issues)

## License

This project is licensed under the [Mozilla Public License 2.0](LICENSE).

