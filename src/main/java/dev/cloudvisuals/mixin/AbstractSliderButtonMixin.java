package dev.cloudvisuals.mixin;

import dev.cloudvisuals.gui.screen.GlassWidgets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SliderWidget.class)
public abstract class AbstractSliderButtonMixin {
   @Shadow
   protected double value;
   @Shadow
   private boolean sliderFocused;

   public AbstractSliderButtonMixin() {
   }

   @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$glassSlider(DrawContext g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      if (GlassWidgets.renderSlider((SliderWidget)(Object)this, g, this.value, this.sliderFocused)) {
         ci.cancel();
      }
   }
}
