# `biosdk-client/`

HTTP `IBioApiV2` (`Client_V_1_0`). `1.4.1-SNAPSHOT`. kernel-core **1.4.1-SNAPSHOT** only. No `kernel-bom`. Jackson 2 via `spring-boot-jackson2`.

Build: `mvn clean install "-Dgpg.skip=true"`
Run: `run-local.bat|sh` → `init|test|all`
Tests: MockWebServer **:9098**. Unset `mosip_biosdk_service` for `mvn test`. Do not start a BioSDK server.

- Ops: DTO → Base64 `request` → `Util.restRequest` → top-level `errors`.
- URLs: `format.url.{format|default}`. Flags: `{FINGER|IRIS|FACE}.format`.
- HttpClient 5, `HttpEntity(HttpHeaders)`, SSL bypass default on. No HttpClient 4.
