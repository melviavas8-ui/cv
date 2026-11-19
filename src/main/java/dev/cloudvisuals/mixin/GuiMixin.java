package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.hud.GlassHotbarModule;
import dev.cloudvisuals.module.impl.hud.GlassScoreboardModule;
import dev.cloudvisuals.module.impl.hud.TitlesModule;
import dev.cloudvisuals.module.impl.visual.CrosshairModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class GuiMixin {
   @Shadow
   private Text title;
   @Shadow
   private int titleRemainTicks;
   @Shadow
   private int titleFadeInTicks;
   @Shadow
   private int titleStayTicks;
   @Shadow
   private int titleFadeOutTicks;
   @Unique
   private boolean cloudvisuals$titleScaled;

   public GuiMixin() {
   }

   @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$hideVanillaEffects(DrawContext guiGraphics, RenderTickCounter deltaTracker, CallbackInfo ci) {
      if (Modules.potions() != null && Modules.potions().hidesVanilla()) {
         ci.cancel();
      }
   }

   @WrapWithCondition(
      method = "renderCrosshair",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"
      )
   )
   private boolean cloudvisuals$customCrosshair(DrawContext guiGraphics, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
      CrosshairModule crosshair = Modules.crosshair();
      if (crosshair != null && crosshair.isEnabled() && sprite.getPath().equals("hud/crosshair")) {
         crosshair.render(guiGraphics);
         return false;
      } else {
         return true;
      }
   }

   @Inject(method = "renderHotbar", at = @At("HEAD"))
   private void cloudvisuals$glassHotbar(DrawContext guiGraphics, RenderTickCounter deltaTracker, CallbackInfo ci) {
      GlassHotbarModule hotbar = Modules.glassHotbar();
      if (hotbar != null && hotbar.isEnabled() && MinecraftClient.getInstance().getCameraEntity() instanceof PlayerEntity player) {
         hotbar.renderBackground(guiGraphics, player);
      }
   }

   @WrapWithCondition(
      method = "renderHotbar",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"
      )
   )
   private boolean cloudvisuals$skipVanillaHotbar(DrawContext guiGraphics, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
      GlassHotbarModule hotbar = Modules.glassHotbar();
      if (hotbar != null && hotbar.isEnabled()) {
         String path = sprite.getPath();
         return !path.startsWith("hud/hotbar") || path.startsWith("hud/hotbar_attack");
      } else {
         return true;
      }
   }

   @Inject(
      method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void cloudvisuals$glassScoreboard(DrawContext guiGraphics, ScoreboardObjective objective, CallbackInfo ci) {
      GlassScoreboardModule scoreboard = Modules.glassScoreboard();
      if (scoreboard != null && scoreboard.isEnabled()) {
         scoreboard.render(guiGraphics, objective);
         ci.cancel();
      }
   }

   @Inject(method = "renderTitleAndSubtitle", at = @At("HEAD"))
   private void cloudvisuals$titleStart(DrawContext guiGraphics, RenderTickCounter deltaTracker, CallbackInfo ci) {
      TitlesModule titles = Modules.titles();
      this.cloudvisuals$titleScaled = titles != null && titles.isEnabled() && this.title != null && this.titleRemainTicks > 0;
      if (this.cloudvisuals$titleScaled) {
         float time = this.titleRemainTicks - deltaTracker.getTickProgress(false);
         float scale = titles.scale(time, this.titleFadeInTicks, this.titleStayTicks, this.titleFadeOutTicks);
         float centerX = guiGraphics.getScaledWindowWidth() / 2.0F;
         float centerY = guiGraphics.getScaledWindowHeight() / 2.0F;
         guiGraphics.getMatrices().pushMatrix();
         guiGraphics.getMatrices().translate(centerX, centerY);
         guiGraphics.getMatrices().scale(scale, scale);
         guiGraphics.getMatrices().translate(-centerX, -centerY);
      }
   }

   @Inject(method = "renderTitleAndSubtitle", at = @At("TAIL"))
   private void cloudvisuals$titleEnd(DrawContext guiGraphics, RenderTickCounter deltaTracker, CallbackInfo ci) {
      if (this.cloudvisuals$titleScaled) {
         guiGraphics.getMatrices().popMatrix();
      }

      this.cloudvisuals$titleScaled = false;
   }
}
