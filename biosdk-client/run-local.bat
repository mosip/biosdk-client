@echo off
setlocal EnableExtensions EnableDelayedExpansion
REM Library runner (no Docker). Linux / macOS / Git Bash: use run-local.sh
REM
REM   run-local.bat init | test | all

set "MODULE_DIR=%~dp0"
if "%MODULE_DIR:~-1%"=="\" set "MODULE_DIR=%MODULE_DIR:~0,-1%"
set "MODULE=biosdk-client"

set "CMD=%~1"
if "%CMD%"=="" goto :usage
if /I "%CMD%"=="-h" goto :usage
if /I "%CMD%"=="--help" goto :usage
if /I "%CMD%"=="help" goto :usage
if /I "%CMD%"=="init" goto :init
if /I "%CMD%"=="test" goto :test
if /I "%CMD%"=="all" goto :all

echo error: unknown command '%CMD%'
goto :usage

:usage
echo Local biosdk-client ^(library — unit tests on MockWebServer :9098^)
echo.
echo   run-local.bat init     package this module ^(skip tests^)
echo   run-local.bat test     Maven unit tests
echo   run-local.bat all      init + test
exit /b 1

:check_prereqs
where java >nul 2>&1
if errorlevel 1 (
  echo error: java is required on PATH
  exit /b 1
)
where mvn >nul 2>&1
if errorlevel 1 (
  echo error: mvn is required on PATH
  exit /b 1
)
exit /b 0

:init
call :check_prereqs
if errorlevel 1 exit /b 1
echo ==^> packaging %MODULE% ^(skip tests^)
pushd "%MODULE_DIR%"
call mvn clean package -DskipTests "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
set "RC=%ERRORLEVEL%"
popd
if not "%RC%"=="0" exit /b %RC%
echo init complete
exit /b 0

:test
call :check_prereqs
if errorlevel 1 exit /b 1
echo ==^> maven unit tests ^(MockWebServer :9098^)
pushd "%MODULE_DIR%"
cmd /S /C "set mosip_biosdk_service=& set MOSIP_BIOSDK_SERVICE=& mvn test -Dgpg.skip=true -Dmaven.javadoc.skip=true"
set "RC=%ERRORLEVEL%"
popd
exit /b %RC%

:all
echo ==^> all: init + test
call :init
if errorlevel 1 exit /b 1
call :test
exit /b %ERRORLEVEL%
