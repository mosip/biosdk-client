# BioSDK Client

[![Maven Package upon a push](https://github.com/mosip/biosdk-client/actions/workflows/push-trigger.yml/badge.svg?branch=develop)](https://github.com/mosip/biosdk-client/actions/workflows/push-trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?branch=develop&project=mosip_biosdk-client&metric=alert_status)](https://sonarcloud.io/dashboard?branch=develop&id=mosip_biosdk-client)

## Overview

**BioSDK Client** is the MOSIP HTTP proxy that implements [`IBioApiV2`](https://github.com/mosip/bio-utils/blob/master/kernel-biometrics-api/src/main/java/io/mosip/kernel/biometrics/spi/IBioApiV2.java). Host services load it as a drop-in BioSDK. Every biometric operation is a `POST` to an external Bio-SDK REST service — this module does **not** run match / extract / segment / quality / convert locally.

Parent: [`../README.md`](../README.md)

Functional spec: [Biometric SDK](https://docs.mosip.io/1.2.0/biometrics/biometric-sdk).

`convertFormat` is `@Deprecated` — use `convertFormatV2`. Do not change `IBioApiV2` method signatures or the JSON envelope (`request` Base64 payload, top-level `errors`).

**Contents:** [Overview](#overview) · [At a glance](#at-a-glance) · [Features](#features) · [Architecture](#architecture) · [IBioApiV2 operations](#ibioapiv2-operations) · [Usage](#usage) · [Configurations](#configurations) · [Tests](#tests) · [Deployment](#deployment) · [License](#license)

---

## At a glance

| Item | Value |
|------|--------|
| Artifact | `io.mosip.biosdk:biosdk-client` |
| JDK / Boot | **21** / Spring Boot **4.1.1** (Framework 7) |
| Main type | `io.mosip.biosdk.client.impl.spec_1_0.Client_V_1_0` |
| SPI | `IBioApiV2` (`kernel-biometrics-api`) |
| HTTP | Apache HttpClient **5** + `RestTemplate`, `HttpEntity(HttpHeaders)` |
| JSON | Jackson **2.x** (not Jackson 3) |
| Packaged as | thin JAR + `*-jar-with-dependencies.jar` (`loader.path`) |
| Tests | JUnit + OkHttp MockWebServer **:9098** |

This module is a **library**. There is no local BioSDK Docker stack.

---

## Features

- `IBioApiV2` implementation (`Client_V_1_0`)
- 1:N match, template extract, segment, quality check, convert (`convertFormatV2`)
- Finger / iris / face via format-specific or default Bio-SDK URLs
- JSON envelope: typed DTO → Base64 `request` → REST → top-level `errors`
- Pooled HttpClient 5 (`disableAutomaticRetries`, connect 5s / socket 30s)
- Optional SSL hostname/trust bypass (default **on**: `restTemplate-ssl-bypass=true`)
- `init()` POSTs `{url}/init` for **each** resolved SDK URL and aggregates `SDKInfo`

This library is **not** a biometric SDK. Caching of templates and retry of HTTP 503 are **not** implemented here.

---

## Architecture

```text
host app  ──loader.path──►  biosdk-client (Client_V_1_0)
                                      │
                                      │  POST /init /match /extract-template
                                      │       /segment /check-quality /convert-format
                                      ▼
                               external Bio-SDK REST
```

Host services set `format.url.default` or `mosip_biosdk_service` at runtime. Local work is Maven + JUnit only.

---

## IBioApiV2 operations

| Method | HTTP path | Notes |
|--------|-----------|--------|
| `init` | `POST {sdkUrl}/init` | Once per distinct URL; host calls `init()` at startup |
| `checkQuality` | `POST …/check-quality` | URL from first modality + flags |
| `match` | `POST …/match` | Gallery + sample |
| `extractTemplate` | `POST …/extract-template` | |
| `segment` | `POST …/segment` | |
| `convertFormat` | `POST …/convert-format` | **Deprecated** |
| `convertFormatV2` | `POST …/convert-format` | Preferred |

Envelope:

```json
{
  "version": "1.0",
  "request": "<Base64 of the operation DTO JSON>"
}
```

Response: nested `{ "response": { "statusCode", "response" } }` or flat `{ "statusCode", "response" }`. Errors are always top-level `errors`.

---

## Usage

This module is a **library** (no Spring Boot `main`). Exercise it with **local JUnit** (MockWebServer). Run scripts from **`biosdk-client/`**.

| You want | Windows cmd | Linux / macOS / Git Bash |
|----------|-------------|--------------------------|
| Help | `run-local.bat` | `./run-local.sh` |
| Package the JAR | `run-local.bat init` | `./run-local.sh init` |
| Unit tests | `run-local.bat test` | `./run-local.sh test` |
| Package + tests | `run-local.bat all` | `./run-local.sh all` |

`bash` on many Windows PCs is **WSL** and does not inherit Windows `JAVA_HOME`. Prefer **cmd** (or Git Bash). PowerShell: `.\run-local.bat test` and quote `"-Dgpg.skip=true"`.

### Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| JDK | **21** | `java -version` |
| Maven | **3.9+** | PowerShell: quote `"-Dgpg.skip=true"` |

No Docker. No PostgreSQL. File logs: `biosdk-client/.local/logs/` (gitignored).

### Windows (cmd)

```bat
cd biosdk-client
run-local.bat init
run-local.bat test
```

### Linux / macOS / Git Bash

```bash
cd biosdk-client
chmod +x run-local.sh
./run-local.sh init
./run-local.sh test
```

### Module runner (`run-local.sh` / `run-local.bat`)

| Command | What it does |
|---------|----------------|
| `init` | `mvn clean package` skip tests |
| `test` | Unit suite on MockWebServer **`:9098`** (unsets `mosip_biosdk_service`) |
| `all` | `init` + `test` |

### Manual Maven

Do **not** leave `MOSIP_BIOSDK_SERVICE` / `mosip_biosdk_service` set when you run the unit suite — the client treats that URL as `format.url.default` and will POST there instead of MockWebServer.

```bat
set MOSIP_BIOSDK_SERVICE=
set mosip_biosdk_service=
```

```text
mvn clean install "-Dgpg.skip=true"
mvn test
mvn test "-Dtest=Client_V_1_0Test"
```

PowerShell: always quote `"-D..."`.

### IDE

There is no Boot `main`. Open the `biosdk-client` Maven module and run JUnit (`Client_V_1_0Test`, `UtilTest`, `DtoTest`, `ExceptionTest`, `ConstantTest`).

### Call `Client_V_1_0` from Java

Host services load the fat JAR via `loader.path`. A host service constructs the client and calls `init` once:

```java
Map<String, String> initParams = new HashMap<>();
initParams.put("format.url.default", "http://localhost:9099/biosdk-service");
// optional: initParams.put("FINGER.format", "minutiae");
// optional: initParams.put("config.parameter.restTemplate-ssl-bypass", "true");

IBioApiV2 client = new Client_V_1_0();
SDKInfo info = client.init(initParams);
Response<QualityCheck> quality = client.checkQuality(sample, modalities, flags);
```

If `format.url.default` is omitted, the client reads env / system property `mosip_biosdk_service`. Set that only on **runtime** hosts — never in the same terminal as `mvn test`.

### Do not

- Do not set `MOSIP_BIOSDK_SERVICE` in User/System environment for laptop unit tests.
- Do not start a BioSDK server for `mvn test`.

### Troubleshooting

| Symptom | What to do |
|---------|------------|
| Unit tests POST to a live URL / expect exception but get HTTP 200 | Unset `MOSIP_BIOSDK_SERVICE` in that terminal, then `run-local.bat test` |
| `ClientRealServerTest` skipped | Expected — default suite is MockWebServer only |

---

## Configurations

### Bio-SDK service URLs

Pass keys on `init(Map<String, String> initParams)`. Keys must contain `format.url.` — the map key after that prefix is the format name:

```text
format.url.default  ->  http://localhost:9099/biosdk-service
format.url.minutiae ->  <Bio-SDK URL for minutiae>
```

Per-call flags select a format for a modality (`BiometricType.name() + ".format"`), for example `FINGER.format=minutiae`.

If `format.url.default` is omitted, the client falls back to env / system property `mosip_biosdk_service`. If that is also empty and no other `format.url.*` keys exist, `init` throws `IllegalStateException`.

```properties
mosip_biosdk_service=http://localhost:9099/biosdk-service
```

Host services set this when they load the fat JAR.

### RestTemplate (system properties)

Set via `initParams` keys `config.parameter.*` (prefix stripped → `System.setProperty`) or JVM `-D`.

| Property | Default | Role |
|----------|---------|------|
| `restTemplate-ssl-bypass` | `true` | Trust-all TLS + no hostname verify. Do not change the default without a release note. |
| `restTemplate-max-connection-per-route` | `20` | HttpClient pool |
| `restTemplate-total-max-connections` | `100` | HttpClient pool |
| `mosip_biosdk_request_response_debug` | unset | `y` logs request/response JSON |

HttpClient 5 automatic retries are **off** (including HTTP 503).

---

## Tests

| Class | When it runs |
|-------|----------------|
| `Client_V_1_0Test`, `UtilTest`, `DtoTest`, `ExceptionTest`, `ConstantTest` | Default `mvn test` / `run-local.bat test` — MockWebServer **9098** |
| `ClientRealServerTest` | Skipped unless `-Dbiosdk.real.server=true` (not part of local unit work) |

`run-local.bat test` / `run-local.sh test` unset `mosip_biosdk_service` in the Maven child process so leftover User env cannot redirect mocks.

---

## Deployment

Host services load the **fat JAR** via `loader.path`, not this module as a Spring Boot process.

### Create install script

1. Create `install.sh`:

```text
#!/bin/sh
set -e
echo "Installing Bio-SDK client.."
export work_dir_env=/
cp biosdk-client-*.jar $work_dir_env
export loader_path_env=biosdk-client-1.4.1-SNAPSHOT-jar-with-dependencies.jar
echo "Installing Bio-SDK client completed."
```

### Prepare biosdk.zip

Place in the same folder:

- `biosdk-client-x.x.x-jar-with-dependencies.jar`
- `install.sh`

```text
zip biosdk.zip biosdk-client-x.x.x-jar-with-dependencies.jar install.sh
```

Deploy the zip to the artifactory / biosdk-service `biosdk_zip_file_path` flow. Infra: [mosip-infra](https://github.com/mosip/mosip-infra).

**Note:** set `mosip_biosdk_service` on containers that use this client.

---

## Documentation

| Doc | Content |
|-----|---------|
| [Biometric SDK](https://docs.mosip.io/1.2.0/biometrics/biometric-sdk) | Product / spec |

---

## Contribution & Community

• To learn how you can contribute code to this application, [click here](https://docs.mosip.io/1.2.0/community/code-contributions).

• If you have questions or encounter issues, visit the [MOSIP Community](https://community.mosip.io/) for support.

• For any GitHub issues: [Report here](https://github.com/mosip/biosdk-client/issues)

---

## License

This project is licensed under the [Mozilla Public License 2.0](../LICENSE).
