package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.cloudvisuals.module.Modules;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapTextureManager.class)
public abstract class LightTextureMixin {
   public LightTextureMixin() {
   }

   @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1))
   private float cloudvisuals$fullbrightGamma(float gamma) {
      return Modules.fullbright() != null ? Modules.fullbright().gamma(gamma) : gamma;
   }
}
