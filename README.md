# MacMicFix

A Minecraft Forge 1.21.1 mod that automatically ensures microphone access on macOS for use with Plasmo Voice and other voice mods.

## Description

MacMicFix automatically detects when Minecraft is running on macOS and checks if the Java process has microphone permissions. If microphone access is not granted, it:

1. Attempts to access the microphone (which triggers the macOS system permission dialog)
2. Displays an informational dialog explaining the need for microphone access

This mod is **client-side only** and does nothing on other operating systems.

## Requirements

- Minecraft 1.21.1
- Forge 52.1.0 or later
- macOS (tested on macOS 12.7.6)

## Installation

1. Download or build the mod JAR (see Building section below)
2. Copy the mod JAR to your Minecraft mods directory:
   ```
   ~/Documents/curseforge/minecraft/Instances/1.21.1/mods/
   ```
3. Launch Minecraft with Forge 1.21.1

## Building

### ⚠️ Important Note for macOS Intel Users

There is a known issue with ForgeGradle 6.0.x on macOS Intel (x86_64) systems that prevents building. The build process tries to download `lwjgl-freetype-3.3.3-natives-macos-patch.jar` which doesn't exist in Maven repositories. **This affects even the official Forge 1.21.1 MDK.**

### Build Options

#### Option 1: GitHub Actions (Recommended)

1. Push this repository to GitHub
2. GitHub Actions will automatically build the mod
3. Download the JAR from the Actions artifacts

#### Option 2: Docker (Works on all platforms)

```bash
docker run --rm -v "$(pwd)":/workspace -w /workspace eclipse-temurin:21-jdk bash -c "./gradlew build --no-daemon"
```

#### Option 3: Native Build (Works on Windows, Linux, macOS ARM)

```bash
./gradlew build
```

The mod JAR will be generated in `build/libs/macmicfix-1.0.0.jar`

For more details about the build issue and workarounds, see [BUILDING.md](BUILDING.md).

## How It Works

When the mod loads on the client side:

1. **macOS Detection**: Checks `System.getProperty("os.name")` to detect macOS
2. **Permission Check**: Attempts to access the microphone using Java's `AudioSystem` API
   - This automatically triggers the macOS system permission dialog if permission isn't granted
3. **User Notification**: Shows an informational dialog via AppleScript explaining how to grant permission in System Settings

### Technical Details

The mod uses `javax.sound.sampled.AudioSystem` to attempt opening a `TargetDataLine`. On macOS, this triggers the system permission dialog if the Java process doesn't have microphone access. The mod then displays a helpful message via AppleScript to guide users to the System Settings.

```java
// Simplified version of the permission check
AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
line.open(format); // This triggers the macOS permission dialog
```

## Logs

The mod logs its actions to the Minecraft log:

- `MacMicFix: Detected macOS, checking microphone permissions...`
- `MacMicFix: Microphone permission is granted` (if permission exists)
- `MacMicFix: Microphone permission not granted, showing informational dialog...` (if permission needed)

## Notes

- The mod only activates on macOS (detected via `System.getProperty("os.name")`)
- The actual microphone permission dialog is shown by macOS, not the mod
- If permission is already granted, the mod logs a message and does nothing else
- The mod does not interfere with other mods or game functionality
- **Client-side only** - no server-side installation needed

## Troubleshooting

### Microphone still not working after installing the mod?

1. Check System Settings → Privacy & Security → Microphone
2. Ensure the Java process has permission
3. If not listed, try manually adding: `~/Documents/curseforge/minecraft/Install/runtime/java-runtime-delta/mac-os/java-runtime-delta/jre.bundle/Contents/Home/bin/java`
4. Restart Minecraft

### Mod not loading?

1. Verify you're using Forge 1.21.1 (version 52.1.0 or later)
2. Check the Minecraft logs for errors
3. Ensure the mod is in the correct mods folder

## License

This mod is provided as-is for personal use.

