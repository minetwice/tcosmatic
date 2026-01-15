package com.tcosmatic;

import com.tcosmatic.cape.CapeManager;
import com.tcosmatic.config.ConfigManager;
import com.tcosmatic.network.NetworkManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcosmaticClient implements ClientModInitializer {
    public static final String MOD_ID = "tcosmatic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static KeyBinding openMenuKey;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Tcosmatic...");
        
        // Initialize managers
        ConfigManager.init();
        CapeManager.init();
        NetworkManager.init();
        
        // Register keybinding
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.tcosmatic.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "category.tcosmatic"
        ));
        
        // Register tick event for keybinding
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new com.tcosmatic.gui.CapeSelectionScreen());
                }
            }
        });
        
        LOGGER.info("Tcosmatic initialized successfully!");
    }
    
    public static KeyBinding getOpenMenuKey() {
        return openMenuKey;
    }
}
