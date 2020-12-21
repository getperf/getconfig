@echo off

set getconfig_home="%~dp0"

python "%~dp0/gcmig.py" %*

exit /b %ERRORLEVEL%
