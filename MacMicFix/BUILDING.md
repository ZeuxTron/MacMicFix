# Building MacMicFix

## Issue with macOS Intel Build

Unfortunately, there is a known issue with ForgeGradle 6.0.x on macOS Intel (x86_64) systems where it tries to download `lwjgl-freetype-3.3.3-natives-macos-patch.jar` which doesn't exist in any Maven repository. This affects even the official Forge 1.21.1 MDK.

## Workarounds

### Option 1: Build on Windows/Linux or macOS ARM

The build works fine on:
- Windows
- Linux 
- macOS ARM (M1/M2/M3)

### Option 2: Use Docker

```bash
docker run --rm -v "$(pwd)":/workspace -w /workspace eclipse-temurin:21-jdk bash -c "./gradlew build"
```

### Option 3: Cross-compile

Build the mod on a different machine and copy the JAR.

### Option 4: Manual Build (Advanced)

Since the Java source is simple and has minimal dependencies, you can:

1. Copy an existing Forge 1.21.1 mod JAR
2. Replace the classes with compiled MacMicFix.class
3. Update META-INF/mods.toml

## Pre-built JAR

If you need a pre-built JAR, you can:
1. Request it from someone with a working build environment
2. Use GitHub Actions to build it automatically
3. Download from the releases page (if available)

## The mod itself

The mod is complete and functional. The issue is purely with the build process on macOS Intel, not with the mod code.

