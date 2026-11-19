package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.hud.GlassTabModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListHud.class)
public abstract class PlayerTabOverlayMixin {
   @Shadow
   @Nullable
   private Text header;
   @Shadow
   @Nullable
   private Text footer;
   @Unique
   private int cloudvisuals$panel;

   public PlayerTabOverlayMixin() {
   }

   @Inject(method = "render", at = @At("HEAD"))
   private void cloudvisuals$startPanels(DrawContext guiGraphics, int width, Scoreboard scoreboard, @Nullable ScoreboardObjective objective, CallbackInfo ci) {
      this.cloudvisuals$panel = 0;
   }

   @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
   private boolean cloudvisuals$glassPanels(DrawContext guiGraphics, int x0, int y0, int x1, int y1, int color) {
      GlassTabModule tab = Modules.glassTab();
      if (tab != null && tab.isEnabled()) {
         if (color == Integer.MIN_VALUE) {
            boolean hasHeader = this.header != null;
            boolean hasFooter = this.footer != null;
            int part = (hasHeader ? 0 : 1) + this.cloudvisuals$panel++;
            boolean top = part == 0 || part == 1 && !hasHeader;
            boolean bottom = part == 2 || part == 1 && !hasFooter;
            tab.drawPanel(guiGraphics, x0, y0, x1, y1, top, bottom);
         }

         return false;
      } else {
         return true;
      }
   }
}
