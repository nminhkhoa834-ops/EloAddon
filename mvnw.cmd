@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

set "WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"
if not exist "%WRAPPER_PROPERTIES%" (
    echo Error: %WRAPPER_PROPERTIES% not found.
    exit /b 1
)

@REM Parse distributionUrl from properties
for /f "tokens=1,* delims==" %%a in ('findstr "distributionUrl" "%WRAPPER_PROPERTIES%"') do set "DIST_URL=%%b"

if "%DIST_URL%"=="" (
    echo Error: distributionUrl not found in %WRAPPER_PROPERTIES%
    exit /b 1
)

@REM Maven user home
if "%MAVEN_USER_HOME%"=="" set "MAVEN_USER_HOME=%USERPROFILE%\.m2"
set "WRAPPER_DIR=%MAVEN_USER_HOME%\wrapper\dists"

@REM Extract Maven version from URL
for %%f in ("%DIST_URL%") do set "DIST_FILE=%%~nf"
@REM Remove -bin suffix
set "DIST_NAME=%DIST_FILE:-bin=%"
set "MAVEN_HOME=%WRAPPER_DIR%\%DIST_NAME%"

@REM Download and extract if needed
if not exist "%MAVEN_HOME%" (
    echo Downloading Maven from: %DIST_URL%
    if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"
    set "TEMP_FILE=%WRAPPER_DIR%\%DIST_FILE%.zip"

    @REM Try PowerShell download
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%WRAPPER_DIR%\%DIST_FILE%.zip'}"
    if errorlevel 1 (
        echo Error: Failed to download Maven.
        exit /b 1
    )

    echo Extracting Maven...
    powershell -Command "& {Expand-Archive -Path '%WRAPPER_DIR%\%DIST_FILE%.zip' -DestinationPath '%WRAPPER_DIR%' -Force}"
    del /q "%WRAPPER_DIR%\%DIST_FILE%.zip" 2>nul
    echo Maven installed to: %MAVEN_HOME%
)

@REM Execute Maven
"%MAVEN_HOME%\bin\mvn.cmd" %*

endlocal

