@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script, Windows.
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "MVNW_REPOURL="
set "MVNW_VERBOSE=false"

set "WRAPPER_JAR=.mvn\\wrapper\\maven-wrapper.jar"
set "WRAPPER_PROPERTIES=.mvn\\wrapper\\maven-wrapper.properties"

if not exist "%WRAPPER_PROPERTIES%" (
  echo [ERROR] %WRAPPER_PROPERTIES% not found.
  exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in ("%WRAPPER_PROPERTIES%") do (
  if "%%A"=="wrapperUrl" set "WRAPPER_URL=%%B"
)

if not defined WRAPPER_URL (
  set "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
)

if not exist "%WRAPPER_JAR%" (
  if not exist ".mvn\\wrapper" mkdir ".mvn\\wrapper" >nul 2>&1
  echo Downloading Maven Wrapper...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ProgressPreference='SilentlyContinue';" ^
    "Invoke-WebRequest -UseBasicParsing -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'" || (
      echo [ERROR] Failed to download Maven Wrapper jar.
      exit /b 1
    )
)

set "MAVEN_OPTS=%MAVEN_OPTS% -Dmaven.multiModuleProjectDirectory=%CD%"

java %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.home=%M2_HOME%" ^
  "-Dmaven.multiModuleProjectDirectory=%CD%" ^
  org.apache.maven.wrapper.MavenWrapperMain %*

endlocal
