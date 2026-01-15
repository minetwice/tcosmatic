package com.tcosmatic.mixin;

import com.tcosmatic.cape.CapeManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class PlayerEntityMixin {
    
    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void injectCustomCape(CallbackInfoReturnable<SkinTextures> cir) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        UUID playerId = player.getUuid();
        
        Identifier customCape = CapeManager.getPlayerCape(playerId);
        
        if (customCape != null) {
            SkinTextures original = cir.getReturnValue();
            SkinTextures modified = new SkinTextures(
                original.texture(),
                original.textureUrl(),
                customCape, // Custom cape
                original.elytraTexture(),
                original.model(),
                original.secure()
            );
            cir.setReturnValue(modified);
        }
    }
}
