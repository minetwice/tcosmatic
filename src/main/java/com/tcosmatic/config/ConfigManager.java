package com.tcosmatic.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tcosmatic.TcosmaticClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    private static Config config;
    
    public static void init() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve("tcosmatic.json");
        load();
    }
    
    public static void load() {
        if (Files.exists(configPath)) {
            try (Reader reader = new FileReader(configPath.toFile())) {
                config = GSON.fromJson(reader, Config.class);
                TcosmaticClient.LOGGER.info("Loaded config from: {}", configPath);
            } catch (Exception e) {
                TcosmaticClient.LOGGER.error("Failed to load config", e);
                config = new Config();
            }
        } else {
            config = new Config();
            save();
        }
    }
    
    public static void save() {
        try (Writer writer = new FileWriter(configPath.toFile())) {
            GSON.toJson(config, writer);
            TcosmaticClient.LOGGER.info("Saved config to: {}", configPath);
        } catch (IOException e) {
            TcosmaticClient.LOGGER.error("Failed to save config", e);
        }
    }
    
    public static Config getConfig() {
        return config;
    }
    
    public static class Config {
        public String selectedCape = "";
        public boolean showCapeButton = true;
        public ButtonPosition buttonPosition = ButtonPosition.TOP_RIGHT;
        
        public Config() {}
        
        public enum ButtonPosition {
            TOP_RIGHT("Top Right"),
            TOP_LEFT("Top Left"),
            BOTTOM_RIGHT("Bottom Right"),
            BOTTOM_LEFT("Bottom Left"),
            CENTER_BELOW("Below Menu");
            
            private final String displayName;
            
            ButtonPosition(String displayName) {
                this.displayName = displayName;
            }
            
            public String getDisplayName() {
                return displayName;
            }
        }
    }
}
