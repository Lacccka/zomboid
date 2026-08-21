@setlocal enableextensions
@cd /d "%~dp0"
SET _JAVA_OPTIONS=
SET PZ_CLASSPATH=./;projectzomboid.jar
".\jre64\bin\java.exe" -Djava.awt.headless=true --enable-native-access=ALL-UNNAMED --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED -Dzomboid.steam=1 -Dzomboid.znetlog=1 -Dorg.lwjgl.util.NoChecks=false -XX:+UseZGC -XX:-CreateCoredumpOnCrash -XX:-OmitStackTraceInFastThrow -Xmx3072m -Djava.library.path=./win64/;./ -cp %PZ_CLASSPATH% zombie.gameStates.MainScreenState -debuglog=Shader
PAUSE
