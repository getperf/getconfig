@echo off

set getconfig_home="%~dp0"

python "%~dp0/gcwinrm.py" %*

exit /b %ERRORLEVEL%
