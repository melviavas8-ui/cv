package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.cloudvisuals.gui.screen.GlassWidgets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TextFieldWidget.class)
public abstract class GlassInputsMixin {
   public GlassInputsMixin() {
   }

   @WrapWithCondition(
      method = "renderWidget",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"
      )
   )
   private boolean cloudvisuals$glassField(DrawContext g, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
      return !GlassWidgets.renderEditBox((ClickableWidget)(Object)this, g, x, y, width, height);
   }
}
