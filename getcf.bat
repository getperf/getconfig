@echo off

set getconfig_home="%~dp0"
set JAVA_OPT="-Dlogback.configurationFile=%~dp0/config/logback.xml"

java -Dgetconfig_home=%~dp0 %JAVA_OPT% -jar "%~dp0/build/libs/getconfig-0.3.14-all.jar" %*

exit /b %ERRORLEVEL%
