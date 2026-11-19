package dev.cloudvisuals.mixin;

import dev.cloudvisuals.gui.screen.GlassWidgets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ScrollableTextFieldWidget;
import net.minecraft.client.gui.widget.ScrollableTextWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScrollableTextFieldWidget.class)
public abstract class AbstractTextAreaWidgetMixin {
   public AbstractTextAreaWidgetMixin() {
   }

   @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$glassPanel(DrawContext g, int x, int y, int width, int height, CallbackInfo ci) {
      if ((Object)this instanceof ScrollableTextWidget widget && GlassWidgets.renderTextPanel(widget, g, x, y, width, height)) {
         ci.cancel();
      }
   }
}
