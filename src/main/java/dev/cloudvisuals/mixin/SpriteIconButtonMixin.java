package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.cloudvisuals.gui.screen.GlassWidgets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TextIconButtonWidget.class)
public abstract class SpriteIconButtonMixin {
   public SpriteIconButtonMixin() {
   }

   @WrapWithCondition(
      method = "drawIcon",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIIF)V"
      )
   )
   private boolean cloudvisuals$icon(DrawContext g, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, float alpha) {
      return !GlassWidgets.replaceSprite((PressableWidget)(Object)this, g, sprite, x, y, width, height);
   }
}
