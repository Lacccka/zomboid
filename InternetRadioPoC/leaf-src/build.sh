#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$SCRIPT_DIR/build"
OUTPUT_JAR="$SCRIPT_DIR/../Contents/mods/LaccckaInternetRadioPoC/leaf/mods/LaccckaInternetRadioBridge.jar"

mkdir -p "$BUILD_DIR/stubs" "$BUILD_DIR/classes" "$(dirname -- "$OUTPUT_JAR")"

java -m jdk.compiler/com.sun.tools.javac.Main --release 17 \
    -d "$BUILD_DIR/stubs" \
    "$SCRIPT_DIR"/compile-stubs/org/spongepowered/asm/mixin/*.java

java -m jdk.compiler/com.sun.tools.javac.Main --release 17 \
    -cp "$BUILD_DIR/stubs" \
    -d "$BUILD_DIR/classes" \
    "$SCRIPT_DIR"/src/main/java/lcc/internetradio/InternetStreamBridge.java \
    "$SCRIPT_DIR"/src/main/java/lcc/internetradio/mixin/FMODSoundEmitterMixin.java

java -m jdk.jartool/sun.tools.jar.Main --create \
    --file "$OUTPUT_JAR" \
    -C "$BUILD_DIR/classes" . \
    -C "$SCRIPT_DIR/src/main/resources" .

echo "Built $OUTPUT_JAR"
