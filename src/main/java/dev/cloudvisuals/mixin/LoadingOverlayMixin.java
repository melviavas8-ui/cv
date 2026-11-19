package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.cloudvisuals.gui.screen.CloudVisualsSplash;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class LoadingOverlayMixin {
   @Shadow
   private float progress;

   public LoadingOverlayMixin() {
   }

   @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/function/IntSupplier;getAsInt()I"))
   private int cloudvisuals$background(int original) {
      return CloudVisualsSplash.active() ? -15987696 : original;
   }

   @WrapOperation(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIFFIIIIIII)V"
      )
   )
   private void cloudvisuals$logo(
      DrawContext g,
      RenderPipeline pipeline,
      Identifier texture,
      int x,
      int y,
      float u,
      float v,
      int width,
      int height,
      int uWidth,
      int vHeight,
      int textureWidth,
      int textureHeight,
      int color,
      Operation<Void> original
   ) {
      if (!CloudVisualsSplash.active()) {
         original.call(new Object[]{g, pipeline, texture, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, color});
      } else if (v == 0.0F) {
         CloudVisualsSplash.draw(g, ColorHelper.getAlpha(color) / 255.0F, this.progress);
      }
   }

   @Inject(method = "renderProgressBar", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$progressBar(DrawContext g, int x0, int y0, int x1, int y1, float alpha, CallbackInfo ci) {
      if (CloudVisualsSplash.active()) {
         ci.cancel();
      }
   }
}
