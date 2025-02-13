@echo off
setlocal enabledelayedexpansion

set BASEDIR=%~dp0
set BASEDIR=%BASEDIR:~0,-1%

docker-compose --file "%BASEDIR%\docker-compose.yml" up
