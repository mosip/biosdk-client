# MOSIP BioSDK Client

HTTP `IBioApiV2` lib. No local biometrics. JDK 21 · Boot 4.1.1.

`biosdk-client/` Java · `.github/` CI

`cd biosdk-client && mvn clean install "-Dgpg.skip=true"` · MockWebServer `:9098` · `build.bat|sh` `init|test|all`. No Docker.

Work: 1 folder · read its `AGENTS.md` · Grep/Glob · short reads · answer first. No subagents, walks, or README/LICENSE/NOTICE/THIRD-PARTY*/`target/`/`.local`/logs/apidocs/`*.iso`/jars/zips unless asked.

Freeze: `IBioApiV2` · Base64 `request` · top-level `errors` · `convertFormatV2` · `HttpEntity(HttpHeaders)` · `spring-boot-jackson2` · no `kernel-bom` · no GPG/`mosip_biosdk_service` secrets.
