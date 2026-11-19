package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.visual.HandModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class SwingDurationMixin {
   public SwingDurationMixin() {
   }

   @ModifyReturnValue(method = "getHandSwingDuration", at = @At("RETURN"))
   private int cloudvisuals$swingDuration(int original) {
      if ((Object)this != MinecraftClient.getInstance().player) {
         return original;
      }

      HandModule module = Modules.hand();
      return module == null ? original : module.swingDuration(original);
   }
}
