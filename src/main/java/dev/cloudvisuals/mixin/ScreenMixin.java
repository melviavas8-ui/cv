package dev.cloudvisuals.mixin;

import dev.cloudvisuals.gui.screen.PauseDesign;
import dev.cloudvisuals.gui.screen.ServerListDesign;
import dev.cloudvisuals.gui.screen.StatusDesign;
import dev.cloudvisuals.render.MenuBackground;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
   public ScreenMixin() {
   }

   @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$customBackground(DrawContext g, float partialTick, CallbackInfo ci) {
      Screen screen = (Screen)(Object)this;
      if (MenuBackground.everywhere() && MenuBackground.draw(g, screen.width, screen.height, 1.0F)) {
         ci.cancel();
      }
   }

   @Inject(method = "renderBackground", at = @At("TAIL"))
   private void cloudvisuals$afterBackground(DrawContext g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      Screen screen = (Screen)(Object)this;
      if (screen instanceof GameMenuScreen pause) {
         PauseDesign.renderHeader(g, pause);
      } else if (screen instanceof MultiplayerScreen join && ServerListDesign.active()) {
         ServerListDesign.title(g, join);
      } else {
         StatusDesign.render(g, screen);
      }
   }
}
