package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.cloudvisuals.gui.screen.GlassWidgets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CheckboxWidget.class)
public abstract class CheckboxMixin {
   @Shadow
   private boolean checked;

   public CheckboxMixin() {
   }

   @WrapWithCondition(
      method = "drawIcon",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIII)V"
      )
   )
   private boolean cloudvisuals$glassBox(DrawContext g, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, int color) {
      return !GlassWidgets.renderCheckbox((CheckboxWidget)(Object)this, g, this.checked, x, y, width);
   }
}
