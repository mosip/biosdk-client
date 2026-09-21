# MOSIP BioSDK Client

Library: HTTP `IBioApiV2`. No local biometrics. JDK 21, Boot 4.1.1.

| Work | Folder |
|------|--------|
| Java | `biosdk-client/` |
| CI | `.github/` |

`cd biosdk-client && mvn clean install "-Dgpg.skip=true"` · tests MockWebServer `:9098` (`run-local.bat test`). No Docker.

**Budget:** one folder. Read only that `AGENTS.md`. Grep/Glob, short reads. No subagents, no repo walks. Skip README, LICENSE, NOTICE, THIRD-PARTY*, `target/`, `.local/`, logs, apidocs, `*.iso`, jars, zips unless asked. Answer first. Short.

**Freeze:** IBioApiV2 + envelope (`request` Base64, top-level `errors`). `convertFormatV2`. `HttpEntity(HttpHeaders)`. `spring-boot-jackson2`. No `kernel-bom`. No GPG keys / `mosip_biosdk_service` secrets.
