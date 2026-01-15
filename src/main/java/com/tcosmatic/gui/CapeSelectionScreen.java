package com.tcosmatic.gui;

import com.tcosmatic.cape.CapeManager;
import com.tcosmatic.config.ConfigManager;
import com.tcosmatic.network.NetworkManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class CapeSelectionScreen extends Screen {
    private static final int CAPE_ICON_SIZE = 48;
    private static final int CAPE_ICON_SPACING = 8;
    private static final int SIDEBAR_WIDTH = 80;
    
    private final List<CapeManager.CapeData> capes;
    private String selectedCape;
    private int scrollOffset = 0;
    
    public CapeSelectionScreen() {
        super(Text.literal("Tcosmatic - Cape Selection"));
        this.capes = new ArrayList<>(CapeManager.getAllCapes());
        this.selectedCape = ConfigManager.getConfig().selectedCape;
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Save button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Save & Quit"),
            button -> saveAndClose()
        ).dimensions(this.width - 120, this.height - 35, 100, 20).build());
        
        // Refresh button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Refresh"),
            button -> refreshCapes()
        ).dimensions(20, this.height - 35, 80, 20).build());
        
        // Remove cape button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Remove Cape"),
            button -> removeCape()
        ).dimensions(110, this.height - 35, 100, 20).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        // Draw background
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        
        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, 
            this.width / 2, 10, 0xFFFFFF);
        
        // Draw sidebar with capes
        renderCapeList(context, mouseX, mouseY);
        
        // Draw character preview
        renderCharacterPreview(context);
        
        // Draw info text
        if (!capes.isEmpty()) {
            String info = "Click a cape to select";
            context.drawCenteredTextWithShadow(this.textRenderer, info, 
                this.width / 2, this.height - 55, 0xAAAAAA);
        } else {
            String info = "No capes found! Add .png files to Tcosmatic/capes folder";
            context.drawCenteredTextWithShadow(this.textRenderer, info, 
                this.width / 2, this.height - 55, 0xFF5555);
        }
    }
    
    private void renderCapeList(DrawContext context, int mouseX, int mouseY) {
        int startX = 10;
        int startY = 40;
        int maxVisibleCapes = (this.height - 100) / (CAPE_ICON_SIZE + CAPE_ICON_SPACING);
        
        // Draw sidebar background
        context.fill(startX - 5, startY - 5, 
            startX + SIDEBAR_WIDTH + 5, this.height - 50, 0x80000000);
        
        for (int i = scrollOffset; i < Math.min(capes.size(), scrollOffset + maxVisibleCapes); i++) {
            CapeManager.CapeData cape = capes.get(i);
            int y = startY + (i - scrollOffset) * (CAPE_ICON_SIZE + CAPE_ICON_SPACING);
            
            boolean isSelected = cape.getName().equals(selectedCape);
            boolean isHovered = mouseX >= startX && mouseX <= startX + CAPE_ICON_SIZE &&
                               mouseY >= y && mouseY <= y + CAPE_ICON_SIZE;
            
            // Draw icon background
            int bgColor = isSelected ? 0xFF4444FF : (isHovered ? 0xFF333333 : 0xFF222222);
            context.fill(startX, y, startX + CAPE_ICON_SIZE, y + CAPE_ICON_SIZE, bgColor);
            
            // Draw cape texture as icon (scale from 22x17 to 40x30)
            // Using the proper cape area coordinates
            int iconPadding = 4;
            int iconWidth = 40;
            int iconHeight = 30;
            
            // Render cape texture (10x16 visible part from 22x17 texture)
            context.drawTexture(
                cape.getTextureId(),
                startX + iconPadding, 
                y + iconPadding + 4,
                0, 0,  // u, v (texture coordinates)
                iconWidth, 
                iconHeight,
                64, 32  // texture width/height (standard cape size)
            );
            
            // Draw border
            int borderColor = isSelected ? 0xFFFFFFFF : 0xFF666666;
            context.drawBorder(startX, y, CAPE_ICON_SIZE, CAPE_ICON_SIZE, borderColor);
            
            // Draw cape name on hover
            if (isHovered) {
                context.drawTooltip(this.textRenderer, Text.literal(cape.getName()), 
                    mouseX, mouseY);
            }
        }
    }
    
    private void renderCharacterPreview(DrawContext context) {
        if (this.client == null || this.client.player == null) return;
        
        int centerX = this.width / 2 + 100;
        int centerY = this.height / 2;
        
        // Draw preview background
        context.fill(centerX - 100, centerY - 120, 
            centerX + 100, centerY + 120, 0x80000000);
        
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("Preview"), centerX, centerY - 110, 0xFFFFFF);
        
        // Render player entity
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(centerX, centerY + 50, 50);
        matrices.scale(50, 50, -50);
        
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf rotation2 = new Quaternionf().rotateX(0.2f);
        rotation.mul(rotation2);
        matrices.multiply(rotation);
        
        DiffuseLighting.method_34742();
        EntityRenderDispatcher dispatcher = this.client.getEntityRenderDispatcher();
        dispatcher.setRenderShadows(false);
        
        dispatcher.render(this.client.player, 0, 0, 0, 0, 1, 
            matrices, context.getVertexConsumers(), 0xF000F0);
        
        dispatcher.setRenderShadows(true);
        matrices.pop();
        DiffuseLighting.enableGuiDepthLighting();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int startX = 10;
            int startY = 40;
            int maxVisibleCapes = (this.height - 100) / (CAPE_ICON_SIZE + CAPE_ICON_SPACING);
            
            for (int i = scrollOffset; i < Math.min(capes.size(), scrollOffset + maxVisibleCapes); i++) {
                int y = startY + (i - scrollOffset) * (CAPE_ICON_SIZE + CAPE_ICON_SPACING);
                
                if (mouseX >= startX && mouseX <= startX + CAPE_ICON_SIZE &&
                    mouseY >= y && mouseY <= y + CAPE_ICON_SIZE) {
                    selectedCape = capes.get(i).getName();
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, capes.size() - ((this.height - 100) / (CAPE_ICON_SIZE + CAPE_ICON_SPACING)));
        
        if (verticalAmount > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (verticalAmount < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }
        
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    private void saveAndClose() {
        ConfigManager.Config config = ConfigManager.getConfig();
        config.selectedCape = selectedCape;
        ConfigManager.save();
        
        if (!selectedCape.isEmpty() && this.client != null && this.client.player != null) {
            CapeManager.setPlayerCape(this.client.player.getUuid(), selectedCape);
            NetworkManager.sendCapeUpdate(selectedCape);
        }
        
        this.close();
    }
    
    private void refreshCapes() {
        CapeManager.loadCapes();
        this.capes.clear();
        this.capes.addAll(CapeManager.getAllCapes());
        scrollOffset = 0;
    }
    
    private void removeCape() {
        selectedCape = "";
        if (this.client != null && this.client.player != null) {
            CapeManager.removePlayerCape(this.client.player.getUuid());
            NetworkManager.sendCapeUpdate("");
        }
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
          }
