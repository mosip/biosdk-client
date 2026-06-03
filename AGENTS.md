# AGENTS.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**biosdk-client** is a Java library implementing [`IBioApiV2`](https://github.com/mosip/bio-utils/blob/master/kernel-biometrics-api/src/main/java/io/mosip/kernel/biometrics/spi/IBioApiV2.java) from the MOSIP kernel biometrics API. Rather than implementing biometric algorithms locally, it acts as an HTTP proxy client that delegates all biometric operations (match, extract, segment, quality check, format convert) to an external Bio-SDK REST service.

It is consumed by MOSIP's ID authentication and ID repository services as a drop-in biometric SDK implementation.

## Build Commands

All commands run from the `biosdk-client/` subdirectory (where `pom.xml` lives):

```bash
# Build and install (skip GPG signing required for local builds)
mvn clean install -Dgpg.skip=true

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=Client_V_1_0Test

# Run a single test method
mvn test -Dtest=Client_V_1_0Test#testConstructorInitialization

# Skip tests during build
mvn clean install -DskipTests -Dgpg.skip=true

# Build the fat JAR (jar-with-dependencies)
mvn clean package -Dgpg.skip=true

# Sonar analysis (requires SONAR_TOKEN)
mvn verify sonar:sonar -Psonar
```

The build produces two JARs in `target/`:
- `biosdk-client-<version>.jar` — thin JAR for embedding in other Spring projects
- `biosdk-client-<version>-jar-with-dependencies.jar` — fat JAR for standalone deployment

## Architecture

### Core Flow

Every biometric operation follows the same pattern in `Client_V_1_0`:
1. Build a typed request DTO (e.g., `MatchRequestDto`, `CheckQualityRequestDto`)
2. Wrap it in a `RequestDto` with the payload Base64-encoded (`Util.base64Encode`)
3. Resolve the target SDK service URL (`getSdkServiceUrl`)
4. POST via `Util.restRequest` (Spring `RestTemplate` with connection pooling)
5. Parse the JSON response, handle errors, deserialize the payload using pre-built `ObjectReader` instances

### URL Resolution

The client supports per-modality and per-format SDK service URLs configured via `initParams`:

```
finger.format.url.minutiae  → SDK URL for minutiae fingerprint format
finger.format.url.default   → Default SDK URL for any finger format
iris.format.url.default     → Default SDK URL for iris
face.format.url.default     → Default SDK URL for face
```

If no URL is found in `initParams`, it falls back to the `mosip_biosdk_service` environment variable / system property. URL resolution logic lives in `getSdkUrls()` and `getSdkServiceUrl()` in `Client_V_1_0`.

### Key Files

| File | Purpose |
|---|---|
| `impl/spec_1_0/Client_V_1_0.java` | Main `IBioApiV2` implementation — all biometric operations |
| `utils/Util.java` | Singleton `RestTemplate` with connection pooling + SSL bypass; `ObjectMapper` singleton; Base64 encoding |
| `constant/ResponseStatus.java` | Status code enum (200 SUCCESS, 401 INVALID_INPUT, 500 UNKNOWN_ERROR, etc.) |
| `exception/BioSdkClientException.java` | Single exception type thrown by all operations |
| `dto/` | Request/response DTO classes (Lombok-annotated) |

### Response Handling

`fillResponse()` in `Client_V_1_0` handles two JSON shapes returned by different SDK service endpoints:
- `{ response: { statusCode, statusMessage, response: {...} } }` — nested
- `{ statusCode, statusMessage, response: {...} }` — flat

Errors are always in a top-level `errors` array; `handleErrors()` → `errorHandler()` throws `BioSdkClientException` if any are present.

### SSL and Connection Pooling

`Util.getRestTemplate()` is a lazily-initialized singleton. SSL certificate validation is **bypassed by default** (`restTemplate-ssl-bypass=true`). Override via system property:
- `restTemplate-ssl-bypass` — `true`/`false`
- `restTemplate-max-connection-per-route` — default 20
- `restTemplate-total-max-connections` — default 100

### Debugging

Set env var `mosip_biosdk_request_response_debug=y` to log full request/response bodies.

### Testing

Tests use OkHttp's `MockWebServer` (started on loopback port 9098) to simulate the Bio-SDK REST service. Test XML fixtures for biometric data are in `src/test/resources/`. `TestUtil` provides helpers for loading XML biometric records via JAXB.

The `convertFormat` method is `@Deprecated`; use `convertFormatV2` instead.

## Runtime Configuration

The `mosip_biosdk_service` env var (or system property) is the global fallback SDK service URL:

```bash
export mosip_biosdk_service=http://localhost:9099/biosdk-service/
```

Config parameters prefixed with `config.parameter.` in `initParams` are set as system properties at `init()` time.