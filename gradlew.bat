@echo off
setlocal
set APP_HOME=%~dp0
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if exist "%CLASSPATH%" (
  if defined JAVA_HOME (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
  ) else (
    set JAVA_EXE=java.exe
  )

  if not exist "%JAVA_EXE%" (
    echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
    exit /b 1
  )

  "%JAVA_EXE%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
  exit /b %ERRORLEVEL%
)

where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)

echo Gradle was not found. Install Gradle or generate the wrapper jar.
exit /b 1
