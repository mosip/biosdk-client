# Java (`biosdk-client/`)

`Client_V_1_0` HTTP `IBioApiV2` `1.4.1-SNAPSHOT`. `kernel-core` 1.4.1-SNAPSHOT compile. `spring-boot-jackson2`. No `kernel-bom`.

`mvn clean install "-Dgpg.skip=true"` · quote `-D` in PowerShell. `build.bat|sh` `init|test|all`. Unset `mosip_biosdk_service` for tests. `:9098`. No BioSDK server.

Envelope: Base64 `request`, top-level `errors`. `format.url.{format|default}` · `{FINGER|IRIS|FACE}.format`. HttpClient 5 · `HttpEntity(HttpHeaders)` · SSL bypass on · no 503 retry · no HC4.
