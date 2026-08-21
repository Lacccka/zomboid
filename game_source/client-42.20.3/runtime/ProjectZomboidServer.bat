@setlocal enableextensions
@cd /d "%~dp0"
SET _JAVA_OPTIONS=
SET PZ_CLASSPATH=./;projectzomboid.jar
".\jre64\bin\java.exe" --enable-native-access=ALL-UNNAMED --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED -XX:+UseZGC -XX:-CreateCoredumpOnCrash -XX:-OmitStackTraceInFastThrow -Xmx3072m -XX:+UseZGC -Djava.library.path=./natives/;./natives/win64/;./ -cp %PZ_CLASSPATH% zombie.network.GameServer
PAUSE
