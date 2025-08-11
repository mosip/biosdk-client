# BioSDK-Client

## Overview

The BioSDK-Client library provides an implementation of [IBioAPIV2](https://github.com/mosip/bio-utils/blob/master/kernel-biometrics-api/src/main/java/io/mosip/kernel/biometrics/spi/IBioApiV2.java), enabling seamless integration with Bio-SDK services for biometric-related functionalities. It supports operations like 1:N matching, segmentation, and extraction, making it a critical component for ID authentication and ID repository services.

It is used by:

* [authentication-internal-service](https://github.com/mosip/id-authentication/tree/master/authentication/authentication-internal-service)
* [authentication-service](https://github.com/mosip/id-authentication/tree/master/authentication/authentication-service)
* [id-repository-identity-service](https://github.com/mosip/id-repository/tree/master/id-repository)

---

## Table of Contents

	* [Overview](#overview)
	* [Prerequisites](#prerequisites)
	* [Setting Up Locally](#setting-up-locally)
	* [Configurations](#configurations)
	* [Deployment](#deployment)
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
	finger.format.url.minutiea -> "<Bio-SDK Service URL for minutiea format>"
```

## Default Format Configuration

For a generic format configuration, use the `.default` suffix:

```text
	finger.format.url.default -> "<Default Bio-SDK Service URL for any unspecified format of finger biomertrics>"
	iris.format.url.default -> "<Default Bio-SDK Service URL for any unspecified format of iris biometrics>"
	face.format.url.default -> "<Default Bio-SDK Service URL for any unspecified format or face biometrics>"
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

**Note:** Set the mosip_biosdk_service environment variable when running Docker containers that use the biosdk-client.

---

## License

This project is licensed under the [MOSIP License](LICENSE).  

---

