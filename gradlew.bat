@echo off
setlocal
if not defined JAVA_HOME (
  echo Set JAVA_HOME to a Java 8 JDK, then run gradlew.bat.
  exit /b 1
)
if not defined GRADLE_USER_HOME set GRADLE_USER_HOME=%~dp0.gradle-user
"%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -classpath "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
endlocal
