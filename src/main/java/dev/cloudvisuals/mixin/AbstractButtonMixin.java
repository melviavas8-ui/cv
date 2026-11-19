package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.cloudvisuals.gui.screen.GlassWidgets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PressableWidget.class)
public abstract class AbstractButtonMixin {
   public AbstractButtonMixin() {
   }

   @WrapOperation(
      method = "renderWidget",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/PressableWidget;drawIcon(Lnet/minecraft/client/gui/DrawContext;IIF)V")
   )
   private void cloudvisuals$glassButton(PressableWidget button, DrawContext g, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
      GlassWidgets.ButtonResult result = GlassWidgets.renderButton(button, g);
      if (result != GlassWidgets.ButtonResult.DONE) {
         if (result == GlassWidgets.ButtonResult.VANILLA) {
            original.call(new Object[]{button, g, mouseX, mouseY, partialTick});
         } else {
            GlassWidgets.skipBackground = true;

            try {
               original.call(new Object[]{button, g, mouseX, mouseY, partialTick});
            } finally {
               GlassWidgets.skipBackground = false;
            }
         }
      }
   }

   @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$noBackground(DrawContext g, CallbackInfo ci) {
      if (GlassWidgets.skipBackground) {
         ci.cancel();
      }
   }
}
