package com.macmicfix;

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
            // Check microphone permission by attempting to access it
            // This will trigger the macOS system permission dialog if permission is not granted
            boolean hasPermission = checkMicrophonePermission();
            
            if (!hasPermission) {
                LOGGER.warn("MacMicFix: Microphone permission not granted, showing informational dialog...");
                
                // Wait a bit to let the system dialog appear first (if it did)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Show informational dialog to user explaining how to grant permission
                showPermissionDialog();
            } else {
                LOGGER.info("MacMicFix: Microphone permission is already granted");
            }
        } catch (Exception e) {
            LOGGER.error("MacMicFix: Error checking microphone permissions", e);
        }
    }

    /**
     * Checks microphone permission by attempting to access the microphone
     * Returns true if permission is granted, false otherwise
     * This will also trigger the macOS permission dialog if permission is not granted
     */
    private boolean checkMicrophonePermission() {
        try {
            // Try to access the microphone device
            // On macOS, this will trigger the permission dialog if permission hasn't been granted
            javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(
                44100, 16, 1, true, false
            );
            
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            
            if (!AudioSystem.isLineSupported(info)) {
                LOGGER.debug("MacMicFix: TargetDataLine not supported");
                return false;
            }
            
            // Try to open the line - this will trigger permission dialog on macOS if needed
            try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.close();
                LOGGER.info("MacMicFix: Microphone permission is granted");
                return true;
            }
        } catch (LineUnavailableException e) {
            // This exception can occur if:
            // 1. Permission is not granted (macOS will show dialog before throwing)
            // 2. Microphone is in use by another application
            LOGGER.debug("MacMicFix: Microphone line unavailable: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.debug("MacMicFix: Error checking microphone permission: {}", e.getMessage());
            return false;
        }
    }


    /**
     * Shows a dialog to the user explaining the need for microphone access
     */
    private void showPermissionDialog() {
        try {
            String script = "display dialog \"Minecraft needs microphone access to use Plasmo Voice. Please allow it in System Settings → Privacy → Microphone.\" buttons {\"OK\"} default button \"OK\" with icon note";
            
            Process process = Runtime.getRuntime().exec(new String[]{
                "osascript", "-e", script
            });
            
            // Wait for the dialog to be dismissed
            process.waitFor();
            
            LOGGER.info("MacMicFix: Permission dialog shown to user");
        } catch (IOException e) {
            LOGGER.error("MacMicFix: Failed to show permission dialog", e);
        } catch (InterruptedException e) {
            LOGGER.error("MacMicFix: Permission dialog interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}

