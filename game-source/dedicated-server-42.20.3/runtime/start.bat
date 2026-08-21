@echo off
setlocal EnableExtensions

cd /d "%~dp0"

REM ============================================================
REM Project Zomboid Dedicated Server
REM Profile: servertest
REM ============================================================

set "SERVER_NAME=servertest"

REM Память сервера
set "SERVER_MIN_RAM=4g"
set "SERVER_MAX_RAM=16g"

set "PZ_CLASSPATH=java/;java/projectzomboid.jar"

echo ============================================================
echo Starting Project Zomboid Dedicated Server
echo Server profile: %SERVER_NAME%
echo RAM: %SERVER_MIN_RAM% - %SERVER_MAX_RAM%
echo ============================================================
echo.

".\jre64\bin\java.exe" ^
-Djava.awt.headless=true ^
-Dzomboid.steam=1 ^
-Dzomboid.znetlog=1 ^
-XX:+UseZGC ^
-XX:-CreateCoredumpOnCrash ^
-XX:-OmitStackTraceInFastThrow ^
-Xms%SERVER_MIN_RAM% ^
-Xmx%SERVER_MAX_RAM% ^
-Djava.library.path=natives/ ^
-cp "%PZ_CLASSPATH%" ^
zombie.network.GameServer ^
-servername "%SERVER_NAME%" ^
-statistic 0 ^
%*

echo.
echo ============================================================
echo Project Zomboid Dedicated Server stopped.
echo ============================================================
pause

endlocal
