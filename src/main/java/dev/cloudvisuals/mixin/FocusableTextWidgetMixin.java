package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.cloudvisuals.gui.screen.GlassWidgets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.NarratedMultilineTextWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NarratedMultilineTextWidget.class)
public abstract class FocusableTextWidgetMixin {
   public FocusableTextWidgetMixin() {
   }

   @WrapWithCondition(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
   private boolean cloudvisuals$glassPanel(DrawContext g, int x0, int y0, int x1, int y1, int color) {
      return !GlassWidgets.renderTextPanel((ClickableWidget)(Object)this, g, x0 - 1, y0, x1 - x0 + 1, y1 - y0);
   }

   @WrapWithCondition(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawStrokedRectangle(IIIII)V"))
   private boolean cloudvisuals$outline(DrawContext g, int x, int y, int width, int height, int color) {
      return !GlassWidgets.active();
   }
}
