package com.tcosmatic.mixin;

import com.tcosmatic.cape.CapeManager;
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
        // Only show button if capes exist
        if (!CapeManager.getAllCapes().isEmpty()) {
            // Position button in top-right corner for easy access
            int buttonWidth = 98;
            int buttonHeight = 20;
            int padding = 4;
            
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("🎨 Tcosmatic"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new CapeSelectionScreen());
                    }
                }
            ).dimensions(
                this.width - buttonWidth - padding,  // Right side
                padding,                              // Top
                buttonWidth, 
                buttonHeight
            ).build());
        }
    }
}
