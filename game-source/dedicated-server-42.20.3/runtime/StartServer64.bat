@echo off
setlocal EnableExtensions

rem ============================================================================
rem Lacccka B42.20 Compatibility Patch
rem Project Zomboid B42.20 Dedicated Server - Windows Launcher
rem
rem Profile:
rem   Dedicated Server + Project Zomboid Client on the same PC
rem   System RAM: 16 GB
rem
rem Safe for:
rem   C:\Program Files (x86)\Steam\...
rem ============================================================================

title Project Zomboid Dedicated Server - Lacccka B42.20

rem ============================================================================
rem Server directory
rem ============================================================================

cd /d "%~dp0"
if errorlevel 1 goto :DIR_ERROR

set "INSTDIR=%CD%"
set "JAVA=%INSTDIR%\jre64\bin\java.exe"

set "JVM_LOG_DIR=%INSTDIR%\logs\jvm"
set "LAUNCHER_LOG=%JVM_LOG_DIR%\launcher.log"

set "PZ_CLASSPATH=java/;java/projectzomboid.jar"

rem ============================================================================
rem Memory profile
rem
rem 16 GB physical RAM
rem Server + game client on the same machine
rem
rem Xms = initial heap
rem SoftMax = preferred ZGC heap size
rem Xmx = absolute maximum heap
rem ============================================================================

set "SERVER_XMS=3g"
set "SERVER_SOFT_MAX=5g"
set "SERVER_XMX=6g"

rem ============================================================================
rem Runtime directories
rem ============================================================================

if exist "%JVM_LOG_DIR%\" goto :JVM_DIR_OK

mkdir "%JVM_LOG_DIR%" >nul 2>&1
if errorlevel 1 goto :JVM_DIR_ERROR

:JVM_DIR_OK

rem ============================================================================
rem Fresh launcher log
rem ============================================================================

>"%LAUNCHER_LOG%" echo %date% %time:~0,8% [LCC][Windows][INFO] Launcher initialized.

call :log INFO "Starting Windows launcher preflight."

rem ============================================================================
rem Java preflight
rem ============================================================================

if not exist "%JAVA%" goto :JAVA_NOT_FOUND

"%JAVA%" -version >nul 2>&1
if errorlevel 1 goto :JAVA_ERROR

call :log OK "Bundled 64-bit Java runtime is available."
call :log OK "Windows compatibility preflight completed."

rem ============================================================================
rem Startup information
rem ============================================================================

echo.
echo ============================================================
echo  Project Zomboid Dedicated Server
echo ============================================================
echo.
echo  Directory:      %INSTDIR%
echo.
echo  JVM logs:       %JVM_LOG_DIR%
echo  Launcher log:   %LAUNCHER_LOG%
echo.
echo  Started:        %date% %time:~0,8%
echo  Server args:    %*
echo.
echo ------------------------------------------------------------
echo  JVM MEMORY PROFILE
echo ------------------------------------------------------------
echo.
echo  Initial heap:   %SERVER_XMS%
echo  Preferred heap: %SERVER_SOFT_MAX%
echo  Maximum heap:   %SERVER_XMX%
echo.
echo  Garbage GC:     ZGC
echo.
echo  Profile:
echo  Server + Game Client on 16 GB RAM
echo.
echo ============================================================
echo  LIVE SERVER LOG
echo ============================================================
echo.

call :log INFO "Starting Project Zomboid dedicated server."
call :log INFO "Server args: %*"
call :log INFO "JVM initial heap: %SERVER_XMS%."
call :log INFO "JVM preferred heap: %SERVER_SOFT_MAX%."
call :log INFO "JVM maximum heap: %SERVER_XMX%."
call :log INFO "Garbage collector: ZGC."

rem ============================================================================
rem Project Zomboid Dedicated Server
rem
rem Java is intentionally launched directly.
rem
rem No START command.
rem No stdout/stderr redirection.
rem
rem This means Project Zomboid LOG/WARN/ERROR output remains visible
rem LIVE in this console.
rem
rem JVM diagnostics:
rem
rem   logs\jvm\gc.log
rem   logs\jvm\hs_err_pid*.log
rem
rem GC log is deliberately less verbose than gc* because full gc*
rem generates a very large amount of diagnostic output during startup.
rem ============================================================================

"%JAVA%" -Djava.awt.headless=true -Dzomboid.steam=1 -Dzomboid.znetlog=1 -XX:+UseZGC -XX:-CreateCoredumpOnCrash -XX:-OmitStackTraceInFastThrow -Xms%SERVER_XMS% -Xmx%SERVER_XMX% -XX:SoftMaxHeapSize=%SERVER_SOFT_MAX% "-Xlog:gc,safepoint:file=logs/jvm/gc.log:time,uptime,level,tags:filecount=5,filesize=20M" "-XX:ErrorFile=logs/jvm/hs_err_pid%%p.log" -Djava.library.path=natives/ -cp "%PZ_CLASSPATH%" zombie.network.GameServer %*

set "SERVER_EXIT_CODE=%ERRORLEVEL%"

call :log INFO "Server process exited with code %SERVER_EXIT_CODE%."

echo.
echo ============================================================
echo  PROJECT ZOMBOID SERVER STOPPED
echo ============================================================
echo.
echo  Exit code: %SERVER_EXIT_CODE%
echo.
echo  Launcher log:
echo  %LAUNCHER_LOG%
echo.
echo  JVM logs:
echo  %JVM_LOG_DIR%
echo.
echo ============================================================
echo.

goto :END


rem ============================================================================
rem Directory error
rem ============================================================================

:DIR_ERROR

echo.
echo ============================================================
echo  LAUNCHER ERROR
echo ============================================================
echo.
echo Could not change to the server directory:
echo.
echo %~dp0
echo.

goto :END


rem ============================================================================
rem JVM directory error
rem ============================================================================

:JVM_DIR_ERROR

echo.
echo ============================================================
echo  LAUNCHER ERROR
echo ============================================================
echo.
echo Could not create JVM log directory:
echo.
echo %JVM_LOG_DIR%
echo.

goto :END


rem ============================================================================
rem Java not found
rem ============================================================================

:JAVA_NOT_FOUND

call :log ERROR "Bundled Java executable was not found."

echo.
echo ============================================================
echo  JAVA NOT FOUND
echo ============================================================
echo.
echo Expected Java executable:
echo.
echo %JAVA%
echo.

goto :END


rem ============================================================================
rem Java failed
rem ============================================================================

:JAVA_ERROR

call :log ERROR "Bundled Java runtime could not be started."

echo.
echo ============================================================
echo  JAVA START FAILED
echo ============================================================
echo.
echo Java executable:
echo.
echo %JAVA%
echo.

goto :END


rem ============================================================================
rem End
rem ============================================================================

:END

echo.
echo ============================================================
echo  Console will remain open.
echo ============================================================
echo.
echo Press any key to close this window...
pause >nul

exit /b


rem ============================================================================
rem Launcher logger
rem ============================================================================

:log

set "LCC_LEVEL=%~1"
set "LCC_MESSAGE=%~2"

echo %date% %time:~0,8% [LCC][Windows][%LCC_LEVEL%] %LCC_MESSAGE%

if not defined LAUNCHER_LOG goto :LOG_DONE

>>"%LAUNCHER_LOG%" echo %date% %time:~0,8% [LCC][Windows][%LCC_LEVEL%] %LCC_MESSAGE%

:LOG_DONE

exit /b 0