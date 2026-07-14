@echo off
rem NULL HORIZON PC client launcher (Windows).
rem Uses the checked-in Gradle wrapper; requires JDK 17+ via JAVA_HOME or PATH.
setlocal
cd /d "%~dp0"
call gradlew.bat --console=plain run %*
endlocal
