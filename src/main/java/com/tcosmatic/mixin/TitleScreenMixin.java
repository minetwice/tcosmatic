package com.tcosmatic.mixin;

import com.tcosmatic.cape.CapeManager;
import com.tcosmatic.config.ConfigManager;
import com.tcosmatic.gui.CapeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    
    protected TitleScreenMixin(Text title) {
        super(title);
    }
    
    @Inject(method = "init", at = @At("RETURN"))
    private void addTcosmaticButton(CallbackInfo ci) {
        // Only show button if capes exist and config allows
        if (!CapeManager.getAllCapes().isEmpty() && 
            ConfigManager.getConfig().showCapeButton) {
            
            int buttonWidth = 98;
            int buttonHeight = 20;
            int padding = 4;
            
            // Get position from config
            ConfigManager.Config.ButtonPosition position = ConfigManager.getConfig().buttonPosition;
            int x, y;
            
            switch (position) {
                case TOP_RIGHT:
                    x = this.width - buttonWidth - padding;
                    y = padding;
                    break;
                    
                case TOP_LEFT:
                    x = padding;
                    y = padding;
                    break;
                    
                case BOTTOM_RIGHT:
                    x = this.width - buttonWidth - padding;
                    y = this.height - buttonHeight - padding;
                    break;
                    
                case BOTTOM_LEFT:
                    x = padding;
                    y = this.height - buttonHeight - padding;
                    break;
                    
                case CENTER_BELOW:
                default:
                    // Below the main menu buttons
                    x = this.width / 2 - buttonWidth / 2;
                    y = this.height / 4 + 48 + 72 + 24;
                    break;
            }
            
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("🎨 Tcosmatic"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new CapeSelectionScreen());
                    }
                }
            ).dimensions(x, y, buttonWidth, buttonHeight).build());
        }
    }
}
