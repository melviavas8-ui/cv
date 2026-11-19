package dev.cloudvisuals.mixin;

import dev.cloudvisuals.module.Modules;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardHandlerMixin {
   public KeyboardHandlerMixin() {
   }

   @Inject(method = "onKey", at = @At("HEAD"))
   private void cloudvisuals$onKeyPress(long window, int action, KeyInput event, CallbackInfo ci) {
      int key = event.key();
      if (action == 1) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (window == mc.getWindow().getHandle()) {
            if (mc.currentScreen instanceof TitleScreen && key == Modules.clickGui().key().get()) {
               Modules.clickGui().onKeyBind();
            } else if (mc.currentScreen == null) {
               Modules.onKeyPressed(key);
            }
         }
      }
   }
}
