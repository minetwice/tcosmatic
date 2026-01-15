package com.tcosmatic.cape;

import com.tcosmatic.TcosmaticClient;
import net.minecraft.client.texture.NativeImage;

public class CapeValidator {
    
    /**
     * Validate if the image can be used as a cape
     */
    public static boolean isValidCape(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Check if image has minimum size
        if (width < 22 || height < 17) {
            TcosmaticClient.LOGGER.warn("Cape image too small: {}x{} (minimum 22x17)", width, height);
            return false;
        }
        
        // Check if image has reasonable maximum size (prevent huge files)
        if (width > 2048 || height > 2048) {
            TcosmaticClient.LOGGER.warn("Cape image too large: {}x{} (maximum 2048x2048)", width, height);
            return false;
        }
        
        return true;
    }
    
    /**
     * Detect cape format type
     */
    public static CapeFormat detectFormat(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Standard formats
        if (width == 64 && height == 32) {
            return CapeFormat.STANDARD;
        }
        if (width == 128 && height == 64) {
            return CapeFormat.HD;
        }
        if (width == 22 && height == 17) {
            return CapeFormat.CAPE_ONLY;
        }
        
        // Check aspect ratio for custom sizes
        double aspectRatio = (double) width / height;
        if (Math.abs(aspectRatio - 2.0) < 0.1) {
            return CapeFormat.STANDARD_SCALED;
        }
        
        // Assume it's a texture with elytra
        return CapeFormat.WITH_ELYTRA;
    }
    
    /**
     * Get recommended scale factor
     */
    public static int getScaleFactor(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Calculate scale from width (standard is 64)
        int widthScale = Math.max(1, width / 64);
        int heightScale = Math.max(1, height / 64);
        
        return Math.min(widthScale, heightScale);
    }
    
    public enum CapeFormat {
        STANDARD("Standard 64x32"),
        HD("HD 128x64"),
        CAPE_ONLY("Cape Only 22x17"),
        STANDARD_SCALED("Scaled Standard"),
        WITH_ELYTRA("With Elytra Parts");
        
        private final String displayName;
        
        CapeFormat(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
