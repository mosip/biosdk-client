# `biosdk-client/`

`Client_V_1_0` HTTP `IBioApiV2`. `1.4.1-SNAPSHOT`. `kernel-core` 1.4.1-SNAPSHOT only. Jackson 2 = `spring-boot-jackson2`. No `kernel-bom`.

`mvn clean install "-Dgpg.skip=true"` · `run-local.bat|sh` `init|test|all` · MockWebServer `:9098`. Unset `mosip_biosdk_service` for tests. No BioSDK server.

Ops: DTO → Base64 `request` → `Util.restRequest` → top-level `errors`. URLs `format.url.{format|default}`. Flags `{FINGER|IRIS|FACE}.format`. HttpClient 5, `HttpEntity(HttpHeaders)`, SSL bypass default on. No HttpClient 4.
