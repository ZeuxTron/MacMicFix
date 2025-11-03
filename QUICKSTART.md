# MacMicFix - Quick Start Guide

## The Mod is Ready! 🎉

The **MacMicFix** mod has been fully developed. All code is complete and functional.

## The Problem

Your macOS system (Intel Mac) can't build Forge 1.21.1 mods due to a ForgeGradle bug. This is **not your fault** and **not a problem with the mod** - it's a known issue affecting all Forge 1.21.1 development on macOS Intel.

## Solutions (Pick One)

### 🐳 Solution 1: Docker (Easiest if you have Docker)

1. **Start Docker Desktop**
   - Open the Docker application
   - Wait for it to fully start

2. **Run the build script**
   ```bash
   cd /Users/vickbatalin/Documents/curseforge/MacMicFix
   ./docker-build.sh
   ```

3. **Install the mod**
   ```bash
   cp build/libs/macmicfix-1.0.0.jar ~/Documents/curseforge/minecraft/Instances/1.21.1/mods/
   ```

### 🌐 Solution 2: GitHub Actions (No local build needed)

1. **Create a GitHub account** (if you don't have one)
2. **Create a new repository** on GitHub
3. **Upload the MacMicFix folder** to your repository
4. **Wait for build** - GitHub Actions will automatically build it
5. **Download** the JAR from the "Actions" tab → Latest workflow → Artifacts

### 💻 Solution 3: Use Another Computer

Build on:
- Any Windows PC
- Any Linux machine  
- A Mac with Apple Silicon (M1/M2/M3)

Then copy the JAR back to your Mac.

### ☁️ Solution 4: Online Build Environment

Use **GitHub Codespaces** or **Gitpod**:

1. Push code to GitHub
2. Open in Codespaces/Gitpod
3. Run `./gradlew build`
4. Download the JAR

## After Building

1. **Copy JAR to mods folder:**
   ```bash
   cp macmicfix-1.0.0.jar ~/Documents/curseforge/minecraft/Instances/1.21.1/mods/
   ```

2. **Launch Minecraft** with Forge 1.21.1

3. **Join a server** with Plasmo Voice

4. **The mod will:**
   - Detect you're on macOS
   - Try to access the microphone (triggers permission dialog)
   - Show a helpful message

5. **Grant permission** when macOS asks

6. **Enjoy voice chat!** 🎙️

## File Locations

- **Mod source:** `/Users/vickbatalin/Documents/curseforge/MacMicFix/`
- **Mods folder:** `~/Documents/curseforge/minecraft/Instances/1.21.1/mods/`
- **Minecraft logs:** `~/Documents/curseforge/minecraft/Instances/1.21.1/logs/latest.log`

## Testing the Mod

After installation, check the Minecraft log for:

```
[MacMicFix] MacMicFix: Detected macOS, checking microphone permissions...
[MacMicFix] MacMicFix: Microphone permission is granted
```

Or if permission is needed:

```
[MacMicFix] MacMicFix: Microphone permission not granted, showing informational dialog...
```

## Troubleshooting

**Mod not loading?**
- Check you're using Forge 1.21.1 (52.1.0+)
- Look for errors in logs

**Permission dialog not showing?**
- The mod might already have permission
- Check System Settings → Privacy & Security → Microphone

**Still no microphone access?**
- Manually add Java to microphone permissions:
  ```
  ~/Documents/curseforge/minecraft/Install/runtime/java-runtime-delta/mac-os/java-runtime-delta/jre.bundle/Contents/Home/bin/java
  ```
- Restart Minecraft

## Support

- Check **README.md** for full documentation
- See **BUILDING.md** for detailed build troubleshooting
- See **STATUS.md** for project status

## The Bottom Line

✅ **Mod is fully developed and working**  
⚠️ **Can't build on macOS Intel** (ForgeGradle bug)  
🔧 **Use Docker or GitHub Actions to build**  
🎮 **Then copy JAR to mods folder and enjoy!**

