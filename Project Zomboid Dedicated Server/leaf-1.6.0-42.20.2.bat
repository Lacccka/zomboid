@echo off

if not defined LCC_KEEP_OPEN (
    set "LCC_KEEP_OPEN=1"
    cmd.exe /k call "%~f0" %*
    exit /b
)

setlocal EnableExtensions EnableDelayedExpansion

title Project Zomboid Dedicated Server - Leaf

cd /d "%~dp0"

set "PZ_CLASSPATH=java/;java/projectzomboid.jar;.leaf\libraries\asm-9.10.1.jar;.leaf\libraries\asm-analysis-9.10.1.jar;.leaf\libraries\asm-commons-9.10.1.jar;.leaf\libraries\asm-tree-9.10.1.jar;.leaf\libraries\asm-util-9.10.1.jar;.leaf\libraries\commons-codec-1.22.0.jar;.leaf\libraries\loader-1.6.0.jar;.leaf\libraries\sponge-mixin-0.17.2+mixin.0.8.7.jar;"

set "LCC_WORKSHOP_JAR=!CD!\steamapps\workshop\content\108600\3783046891\mods\LaccckaInternetRadioPoC\leaf\mods\LaccckaInternetRadioServerBridge.jar"
set "LCC_RUNTIME_DIR=!CD!\.leaf\runtime-mods"
set "LCC_RUNTIME_JAR=!LCC_RUNTIME_DIR!\LaccckaInternetRadioServerBridge.jar"

echo.
echo ============================================================
echo Project Zomboid Dedicated Server with Leaf
echo ============================================================
echo.

if not exist ".\jre64\bin\java.exe" (
    echo ERROR: Java was not found:
    echo !CD!\jre64\bin\java.exe
    goto ERROR_END
)

if not exist "!LCC_WORKSHOP_JAR!" (
    echo ERROR: Workshop bridge was not found:
    echo !LCC_WORKSHOP_JAR!
    echo.
    echo Start the normal server once to download Workshop item 3783046891.
    goto ERROR_END
)

if not exist "!LCC_RUNTIME_DIR!" (
    mkdir "!LCC_RUNTIME_DIR!"

    if errorlevel 1 (
        echo ERROR: Failed to create:
        echo !LCC_RUNTIME_DIR!
        goto ERROR_END
    )
)

echo Copying the Internet Radio bridge...
echo FROM: !LCC_WORKSHOP_JAR!
echo TO:   !LCC_RUNTIME_JAR!
echo.

copy /Y "!LCC_WORKSHOP_JAR!" "!LCC_RUNTIME_JAR!" >nul

if errorlevel 1 (
    echo ERROR: Failed to copy the Internet Radio bridge.
    goto ERROR_END
)

echo Internet Radio bridge copied successfully.
echo.
echo Starting server...
echo.

".\jre64\bin\java.exe" -Djava.awt.headless=true -Dzomboid.steam=1 -Dzomboid.znetlog=1 --enable-native-access=ALL-UNNAMED -XX:+UseZGC -XX:-CreateCoredumpOnCrash -XX:-OmitStackTraceInFastThrow -Xms16g -Xmx16g -Djava.library.path=natives/ "-Dleaf.addMods=!LCC_RUNTIME_JAR!" "-Dleaf.gameWorkshopPath=!CD!\steamapps\workshop\content\108600" -cp "!PZ_CLASSPATH!" dev.aoqia.leaf.loader.impl.launch.knot.KnotServer -statistic 0 %1 %2

set "SERVER_EXIT=!ERRORLEVEL!"

echo.
echo ============================================================
echo Server process stopped.
echo Exit code: !SERVER_EXIT!
echo ============================================================
echo.
pause

endlocal
goto :eof

:ERROR_END

echo.
echo ============================================================
echo Server was not started.
echo ============================================================
echo.
pause

endlocal
goto :eof