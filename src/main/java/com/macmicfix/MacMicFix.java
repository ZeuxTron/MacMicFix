package com.macmicfix;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;

@Mod(MacMicFix.MODID)
public class MacMicFix {
    public static final String MODID = "macmicfix";
    private static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public MacMicFix() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (isMacOS()) {
                LOGGER.info("MacMicFix: Detected macOS, checking microphone permissions...");
                checkAndRequestMicrophonePermission();
            } else {
                LOGGER.info("MacMicFix: Not macOS, skipping microphone permission check");
            }
        });
    }

    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        String message = event.getMessage();
        if (message.equalsIgnoreCase("/macmicfix") || message.equalsIgnoreCase("/micfix")) {
            event.setCanceled(true);
            
            if (isMacOS()) {
                LOGGER.info("MacMicFix: User requested microphone permission instructions");
                
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.sendSystemMessage(Component.literal("§e[MacMicFix] Showing microphone permission instructions..."));
                }
                
                // Show dialog in a separate thread to not block the game
                new Thread(() -> {
                    showPermissionDialog();
                }).start();
            } else {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.sendSystemMessage(Component.literal("§c[MacMicFix] This mod only works on macOS!"));
                }
            }
        }
    }

    /**
     * Checks if the current operating system is macOS
     */
    private boolean isMacOS() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        return osName.contains("mac") || osName.contains("darwin");
    }

    /**
     * Checks if microphone permission is granted and prompts user if needed
     */
    private void checkAndRequestMicrophonePermission() {
        try {
            LOGGER.warn("MacMicFix: IMPORTANT - Java apps on macOS cannot automatically request microphone permission!");
            LOGGER.warn("MacMicFix: You must MANUALLY grant permission in System Settings");
            
            // Always show the dialog on macOS because Java AudioSystem check is unreliable
            // macOS blocks microphone access at system level even if AudioSystem thinks it has access
            LOGGER.info("MacMicFix: Showing instructions to user...");
            
            // Show informational dialog to user explaining how to grant permission
            showPermissionDialog();
            
            LOGGER.info("MacMicFix: To see these instructions again, type /macmicfix in chat");
        } catch (Exception e) {
            LOGGER.error("MacMicFix: Error showing microphone permission instructions", e);
        }
    }

    /**
     * Checks microphone permission by attempting to access the microphone
     * Returns true if permission is granted, false otherwise
     * Attempts to trigger the macOS permission dialog if permission is not granted
     */
    private boolean checkMicrophonePermission() {
        LOGGER.info("MacMicFix: Checking microphone permissions...");
        
        // First, try to trigger permission dialog using native macOS command
        // This is more reliable than Java AudioSystem on macOS
        try {
            LOGGER.info("MacMicFix: Attempting to trigger microphone permission dialog using native command...");
            
            // Use a short AppleScript that tries to access the microphone
            // This will trigger the system permission dialog
            String script = "try\n" +
                    "    set audioInput to (load script POSIX file \"/System/Library/ScriptingAdditions/StandardAdditions.osax\")\n" +
                    "    -- Attempt to access microphone\n" +
                    "end try";
            
            ProcessBuilder pb = new ProcessBuilder("osascript", "-e", 
                "do shell script \"ffmpeg -f avfoundation -i :0 -t 0.1 -f null - 2>&1 || true\"");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Wait a bit for the dialog to potentially appear
            Thread.sleep(500);
            process.destroy();
            
            LOGGER.info("MacMicFix: Native command executed");
        } catch (Exception e) {
            LOGGER.debug("MacMicFix: Native command approach failed: {}", e.getMessage());
        }
        
        // Now check if we can actually access the microphone
        try {
            javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(
                44100.0f, 16, 1, true, false
            );
            
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            
            if (!AudioSystem.isLineSupported(info)) {
                LOGGER.warn("MacMicFix: TargetDataLine not supported by AudioSystem");
                return false;
            }
            
            TargetDataLine line = null;
            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                
                // Opening the line is not enough - we need to actually try to START it
                // On macOS, this will trigger the permission check
                LOGGER.info("MacMicFix: Line opened, now attempting to start recording...");
                line.start();
                
                // Try to actually read some data from the microphone
                byte[] buffer = new byte[4096];
                int bytesRead = line.read(buffer, 0, buffer.length);
                
                if (bytesRead > 0) {
                    LOGGER.info("MacMicFix: Successfully read {} bytes from microphone - permission is GRANTED!", bytesRead);
                    return true;
                } else {
                    LOGGER.warn("MacMicFix: Could not read from microphone - permission likely DENIED");
                    return false;
                }
            } catch (LineUnavailableException e) {
                LOGGER.warn("MacMicFix: Could not open microphone line: {}", e.getMessage());
                LOGGER.warn("MacMicFix: Microphone permission is likely NOT granted");
                return false;
            } catch (SecurityException e) {
                LOGGER.error("MacMicFix: Security exception - microphone access DENIED: {}", e.getMessage());
                return false;
            } catch (Exception e) {
                LOGGER.warn("MacMicFix: Error accessing microphone: {}", e.getMessage());
                return false;
            } finally {
                if (line != null) {
                    if (line.isActive()) {
                        line.stop();
                    }
                    if (line.isOpen()) {
                        line.close();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("MacMicFix: Error checking microphone permission: {}", e.getMessage());
            return false;
        }
    }


    /**
     * Shows a dialog to the user explaining the need for microphone access
     */
    private void showPermissionDialog() {
        try {
            // Get the Java runtime path for instructions
            String javaPath = System.getProperty("java.home") + "/bin/java";
            
            String message = "⚠️ Minecraft needs microphone access for Plasmo Voice!\\n\\n" +
                    "Java apps CANNOT auto-request permission on macOS.\\n" +
                    "You MUST manually grant access:\\n\\n" +
                    "1. Click 'Open System Settings' below\\n" +
                    "2. Find 'Java' in the Microphone list\\n" +
                    "3. Enable the checkbox\\n" +
                    "4. Restart Minecraft\\n\\n" +
                    "Java path (if needed):\\n" + javaPath + "\\n\\n" +
                    "Type /macmicfix in chat to see this again.";
            
            String script = "display dialog \"" + message + "\" buttons {\"Open System Settings\", \"OK\"} default button \"Open System Settings\" with icon caution";
            
            ProcessBuilder pb = new ProcessBuilder("osascript", "-e", script);
            Process process = pb.start();
            
            // Check which button was clicked
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // User clicked "Open System Settings"
                LOGGER.info("MacMicFix: Opening System Settings for user...");
                Runtime.getRuntime().exec(new String[]{"open", "x-apple.systempreferences:com.apple.preference.security?Privacy_Microphone"});
            }
            
            LOGGER.info("MacMicFix: Permission instructions shown to user");
        } catch (IOException e) {
            LOGGER.error("MacMicFix: Failed to show permission dialog", e);
        } catch (InterruptedException e) {
            LOGGER.error("MacMicFix: Permission dialog interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}

