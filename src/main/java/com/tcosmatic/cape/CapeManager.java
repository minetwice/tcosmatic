package com.tcosmatic.cape;

import com.tcosmatic.TcosmaticClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CapeManager {
    private static final Map<String, CapeData> capes = new ConcurrentHashMap<>();
    private static final Map<UUID, Identifier> playerCapes = new ConcurrentHashMap<>();
    private static Path capesDirectory;
    
    public static void init() {
        capesDirectory = FabricLoader.getInstance().getGameDir().resolve("Tcosmatic").resolve("capes");
        
        try {
            Files.createDirectories(capesDirectory);
            TcosmaticClient.LOGGER.info("Capes directory created at: {}", capesDirectory);
        } catch (IOException e) {
            TcosmaticClient.LOGGER.error("Failed to create capes directory", e);
        }
        
        loadCapes();
    }
    
    public static void loadCapes() {
        capes.clear();
        
        File capesDir = capesDirectory.toFile();
        if (!capesDir.exists() || !capesDir.isDirectory()) {
            return;
        }
        
        File[] capeFiles = capesDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".png"));
        
        if (capeFiles == null) return;
        
        for (File capeFile : capeFiles) {
            try {
                loadCape(capeFile);
            } catch (Exception e) {
                TcosmaticClient.LOGGER.error("Failed to load cape: {}", capeFile.getName(), e);
            }
        }
        
        TcosmaticClient.LOGGER.info("Loaded {} capes", capes.size());
    }
    
    private static void loadCape(File file) throws IOException {
        String name = file.getName().replace(".png", "");
        
        try (FileInputStream fis = new FileInputStream(file)) {
            NativeImage originalImage = NativeImage.read(fis);
            
            // Validate cape
            if (!CapeValidator.isValidCape(originalImage)) {
                TcosmaticClient.LOGGER.error("Invalid cape format: {}", name);
                originalImage.close();
                return;
            }
            
            // Detect format
            CapeValidator.CapeFormat format = CapeValidator.detectFormat(originalImage);
            TcosmaticClient.LOGGER.info("Detected cape format: {} for {}", 
                format.getDisplayName(), name);
            
            // Process image to extract only cape portion
            NativeImage processedImage = processCapeImage(originalImage);
            
            MinecraftClient client = MinecraftClient.getInstance();
            Identifier textureId = Identifier.of(TcosmaticClient.MOD_ID, "capes/" + name);
            
            client.execute(() -> {
                client.getTextureManager().registerTexture(
                    textureId, 
                    new NativeImageBackedTexture(processedImage)
                );
            });
            
            CapeData capeData = new CapeData(name, textureId, file.getAbsolutePath());
            capes.put(name, capeData);
            
            TcosmaticClient.LOGGER.info("Loaded cape: {} ({}x{} -> {}x{})", name, 
                originalImage.getWidth(), originalImage.getHeight(),
                processedImage.getWidth(), processedImage.getHeight());
            
            // Close original if different from processed
            if (originalImage != processedImage) {
                originalImage.close();
            }
        }
    }
    
    /**
     * Process cape image to handle various formats:
     * - 64x32 (standard cape)
     * - 128x64 (HD cape)  
     * - 22x17 (cape only from larger texture)
     * - Larger textures with elytra (extracts cape portion only)
     */
    private static NativeImage processCapeImage(NativeImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        
        TcosmaticClient.LOGGER.info("Processing cape image: {}x{}", width, height);
        
        // Standard cape sizes - use as is
        if ((width == 64 && height == 32) || (width == 128 && height == 64)) {
            TcosmaticClient.LOGGER.info("Standard cape format detected");
            return original;
        }
        
        // Calculate scale factor
        int scale = Math.max(width / 64, height / 64);
        if (scale < 1) scale = 1;
        
        // Cape dimensions in the texture (standard is 10x16, positioned at 0,0)
        // But the full cape texture area is 22x17
        int capeWidth = 22 * scale;
        int capeHeight = 17 * scale;
        
        // Full standard size for output
        int outputWidth = 64 * scale;
        int outputHeight = 32 * scale;
        
        // Check if image is large enough to contain cape
        if (width < capeWidth || height < capeHeight) {
            TcosmaticClient.LOGGER.warn("Image too small for cape extraction, using as-is");
            return original;
        }
        
        // Create new image with standard cape dimensions
        NativeImage processed = new NativeImage(outputWidth, outputHeight, true);
        
        // Fill with transparent pixels
        for (int x = 0; x < outputWidth; x++) {
            for (int y = 0; y < outputHeight; y++) {
                processed.setColor(x, y, 0); // Fully transparent
            }
        }
        
        // Copy cape portion (22x17 area from top-left)
        // This is where the actual cape texture is located
        for (int x = 0; x < Math.min(capeWidth, width); x++) {
            for (int y = 0; y < Math.min(capeHeight, height); y++) {
                int color = original.getColor(x, y);
                processed.setColor(x, y, color);
            }
        }
        
        TcosmaticClient.LOGGER.info("Processed cape to: {}x{} (extracted {}x{} cape area)", 
            outputWidth, outputHeight, capeWidth, capeHeight);
        
        return processed;
    }
    
    public static Collection<CapeData> getAllCapes() {
        return new ArrayList<>(capes.values());
    }
    
    public static CapeData getCape(String name) {
        return capes.get(name);
    }
    
    public static void setPlayerCape(UUID playerId, String capeName) {
        CapeData cape = capes.get(capeName);
        if (cape != null) {
            playerCapes.put(playerId, cape.getTextureId());
        }
    }
    
    public static Identifier getPlayerCape(UUID playerId) {
        return playerCapes.get(playerId);
    }
    
    public static void removePlayerCape(UUID playerId) {
        playerCapes.remove(playerId);
    }
    
    public static Path getCapesDirectory() {
        return capesDirectory;
    }
    
    public static class CapeData {
        private final String name;
        private final Identifier textureId;
        private final String filePath;
        
        public CapeData(String name, Identifier textureId, String filePath) {
            this.name = name;
            this.textureId = textureId;
            this.filePath = filePath;
        }
        
        public String getName() {
            return name;
        }
        
        public Identifier getTextureId() {
            return textureId;
        }
        
        public String getFilePath() {
            return filePath;
        }
    }
}
