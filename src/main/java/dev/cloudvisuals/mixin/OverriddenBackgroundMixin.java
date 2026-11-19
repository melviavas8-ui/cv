package dev.cloudvisuals.mixin;

import dev.cloudvisuals.gui.screen.StatusDesign;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MessageScreen.class, LevelLoadingScreen.class})
public abstract class OverriddenBackgroundMixin {
   public OverriddenBackgroundMixin() {
   }

   @Inject(method = "renderBackground", at = @At("TAIL"))
   private void cloudvisuals$card(DrawContext g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      StatusDesign.render(g, (Screen)(Object)this);
   }
}
