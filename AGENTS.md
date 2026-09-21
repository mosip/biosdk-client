# MOSIP BioSDK Client

IBioApiV2 HTTP proxy library. **No local biometric algorithms.** JDK 21, Boot 4.1.1.

| Work | Folder |
|------|--------|
| Java / Maven | `biosdk-client/` |
| CI | `.github/` |

Build: `cd biosdk-client && mvn clean install "-Dgpg.skip=true"`
Tests: MockWebServer **:9098** (`mvn test` / `run-local.bat test`). No Docker stack.

**Budget:** one folder; that folder’s `AGENTS.md` only. Grep/Glob first. Short reads. No subagents. Do not open README, LICENSE, NOTICE, THIRD-PARTY*, `target/`, `.local/`, logs, apidocs unless asked. Skip `*.iso`, `*.jar`, `*.zip`, javadoc. Answer first. Short.

**Freeze:** IBioApiV2 + envelope (`request` Base64, top-level `errors`). `convertFormatV2`. `HttpEntity(HttpHeaders)`. No `kernel-bom`. No GPG keys / `mosip_biosdk_service` secrets.
