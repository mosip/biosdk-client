# BioSDK-Client

## Overview

The **BioSDK-Client** library provides an implementation of [IBioApiV2](https://github.com/mosip/bio-utils/blob/master/kernel-biometrics-api/src/main/java/io/mosip/kernel/biometrics/spi/IBioApiV2.java), enabling seamless integration with Bio-SDK services for biometric-related functionalities. It supports operations like 1:N matching, segmentation, and extraction, making it a critical component for ID authentication and ID repository services.

For a complete functional overview and capabilities, refer to the **[official documentation](https://docs.mosip.io/1.2.0/biometrics/biometric-sdk)**.

### Used By

This library is used by the following MOSIP services:

* [authentication-internal-service](https://github.com/mosip/id-authentication/tree/master/authentication/authentication-internal-service)
* [authentication-service](https://github.com/mosip/id-authentication/tree/master/authentication/authentication-service)
* [id-repository-identity-service](https://github.com/mosip/id-repository/tree/master/id-repository)

---

## Features

- Implementation of IBioApiV2 interface
- 1:N biometric matching
- Biometric segmentation and extraction
- Quality assessment and validation
- Multi-modal biometric support (fingerprint, iris, face)
- Format-specific and default URL configuration
- Integration with external Bio-SDK services
- Retry and fault-tolerance mechanisms
- Caching for improved performance

---

## Table of Contents

* [Overview](#overview)
* [Features](#features)
* [Prerequisites](#prerequisites)
* [Setting Up Locally](#setting-up-locally)
* [Configurations](#configurations)
* [Deployment](#deployment)
* [Upgrade](#upgrade)
* [Documentation](#documentation)
* [Contribution & Community](#contribution--community)
* [License](#license)

---

## Prerequisites

Ensure you have the following dependencies installed before proceeding:

	1. **Java**: Version 21.0.3
	2. **Maven**: Version 3.9.6 for building the project.
	3. **Git**: To clone the repository
	4. **Postman (optional)**: For testing the APIs

---

## Setting Up Locally

### Follow these steps to set up the BioSDK-Client locally:

1. **Clone the repository**

```bash
   git clone https://github.com/mosip/biosdk-client.git
   cd biosdk-client
```

2. **Build the project using Maven to resolve dependencies**

```bash
   mvn clean install -Dgpg.skip=true
```
---

## Configurations

## Bio-SDK Service URLs

Bio-SDK service URLs can vary based on the modality and format. Specify these in the `initParams` of the `init` method as key-value pairs.

For example, to configure the URL for the minutiae format of fingerprints:

```text
	finger.format.url.minutiae -> "<Bio-SDK Service URL for minutiae format>"
```

## Default Format Configuration

For a generic format configuration, use the `.default` suffix:

```text
	finger.format.url.default -> "<Default Bio-SDK Service URL for any unspecified format of finger biometrics>"
	iris.format.url.default -> "<Default Bio-SDK Service URL for any unspecified format of iris biometrics>"
	face.format.url.default -> "<Default Bio-SDK Service URL for any unspecified format of face biometrics>"
```

If the above URLs are not specified in initParams, it will take a default Bio-SDK service URL from below property.


## Default URL


If the above URLs are not specified in initParams, the system will fallback to a global default URL:

```properties
	mosip_biosdk_service=<Bio SDK service url>
```

for example:

```properties
	mosip_biosdk_service=http://localhost:9099/biosdk-service/
```
---

## Deployment

There are multiple ways to deploy biosdk-client with mosip-services. According to mosip-infra 1.1.3, it can be deployed using the following steps.

### Create install script

1. Create a bash script file named "install.sh", as shown below:

```sh
	#!/bin/bash

	#installs the Bio-SDK
	set -e

	echo "Installating Mock Bio-SDK.."

	export work_dir_env=/

	cp biosdk-client-*.jar $work_dir_env

	export loader_path_env=biosdk-client-1.1.3-jar-with-dependencies.jar

	echo "Installating Mock Bio-SDK completed."
```

### Prepare biosdk.zip

1. Place the following files in the same folder:

  * biosdk-client-x.x.x-jar-with-dependencies.jar
  * install.sh

2.  Create a ZIP file:

```bash
zip biosdk.zip biosdk-client-x.x.x-jar-with-dependencies.jar install.sh
```

3. Deploy the `biosdk.zip` file to the artifactory docker container.


For more information on deployment, please refer to [mosip-infra](https://github.com/mosip/mosip-infra) README file.

**Note:** Set the `mosip_biosdk_service` environment variable when running Docker containers that use the biosdk-client.

---

## Upgrade

### Upgrade Steps

1. Backup your current configuration from config server
2. Stop the dependent services (ID Authentication, ID Repository)
3. Update the biosdk-client version in your project's POM file
4. Rebuild and redeploy dependent services with the updated JAR:
   ```bash
   mvn clean install -Dgpg.skip=true
   ```
5. Verify logs and health endpoints
6. Run validation tests to ensure biometric operations are working correctly

For major version upgrades, refer to the [MOSIP Release Notes](https://docs.mosip.io/1.2.0/releases) for breaking changes.

---

## Documentation

For more detailed documentation, check the [docs](docs) directory (if available).

### API Documentation

API interfaces and usage details:

- **IBioApiV2 Interface**: [GitHub Source](https://github.com/mosip/bio-utils/blob/master/kernel-biometrics-api/src/main/java/io/mosip/kernel/biometrics/spi/IBioApiV2.java)
- **GitHub Pages**: [MOSIP API Documentation](https://mosip.github.io/documentation/)

### Product Documentation

To learn more about Bio-SDK Client from a functional perspective and use case scenarios, refer to our main documentation: [Biometric SDK](https://docs.mosip.io/1.2.0/biometrics/biometric-sdk).

---

## Contribution & Community

• To learn how you can contribute code to this application, [click here](https://docs.mosip.io/1.2.0/community/code-contributions).

• If you have questions or encounter issues, visit the [MOSIP Community](https://community.mosip.io/) for support.

• For any GitHub issues: [Report here](https://github.com/mosip/biosdk-client/issues)

---

## License

This project is licensed under the [Mozilla Public License 2.0](LICENSE).  

---
