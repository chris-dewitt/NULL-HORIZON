@echo off
REM NULL HORIZON PC launcher (Windows)
setlocal EnableExtensions

cd /d "%~dp0"

REM Prefer a JDK 17/21 install if present — more reliable than JDK 25 for tooling.
if exist "C:\Program Files\Eclipse Adoptium\jdk-17*" (
  for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do set "JAVA_HOME=%%~fD"
)
if not defined JAVA_HOME if exist "C:\Program Files\Eclipse Adoptium\jdk-21*" (
  for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do set "JAVA_HOME=%%~fD"
)

if defined JAVA_HOME (
  set "PATH=%JAVA_HOME%\bin;%PATH%"
  echo Using JAVA_HOME=%JAVA_HOME%
) else (
  echo JAVA_HOME not overridden. Using whatever java is on PATH.
)

call "%~dp0gradlew.bat" --stop >nul 2>&1
call "%~dp0gradlew.bat" run
exit /b %ERRORLEVEL%
