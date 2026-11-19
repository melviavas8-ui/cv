package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.cloudvisuals.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
   public LivingEntityRendererMixin() {
   }

   @ModifyReturnValue(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("RETURN"))
   private boolean cloudvisuals$showOwnName(boolean show, LivingEntity entity, double distance) {
      if (!show && Modules.nametags() != null && Modules.nametags().showSelf()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         return entity == mc.player
            && entity == mc.getCameraEntity()
            && !mc.options.getPerspective().isFirstPerson()
            && MinecraftClient.isHudEnabled()
            && !entity.isInvisible();
      } else {
         return show;
      }
   }
}
