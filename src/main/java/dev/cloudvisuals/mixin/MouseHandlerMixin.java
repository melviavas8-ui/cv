package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.cloudvisuals.hud.GameStats;
import dev.cloudvisuals.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseHandlerMixin {
   public MouseHandlerMixin() {
   }

   @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$zoomWheel(long window, double horizontal, double vertical, CallbackInfo ci) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (window == mc.getWindow().getHandle() && mc.currentScreen == null && Modules.zoom() != null && Modules.zoom().scroll(vertical)) {
         ci.cancel();
      }
   }

   @ModifyExpressionValue(
      method = "updateMouse",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 0)
   )
   private Object cloudvisuals$zoomSensitivity(Object sensitivity) {
      return sensitivity instanceof Double value && Modules.zoom() != null ? Modules.zoom().sensitivity(value) : sensitivity;
   }

   @Inject(method = "onMouseButton", at = @At("HEAD"))
   private void cloudvisuals$countClick(long window, MouseInput info, int action, CallbackInfo ci) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (action == 1 && window == mc.getWindow().getHandle() && mc.currentScreen == null) {
         GameStats.mousePressed(info.button());
      }
   }
}
