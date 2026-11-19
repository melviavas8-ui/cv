package dev.cloudvisuals.mixin;

import dev.cloudvisuals.gui.screen.GlassWidgets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.LockButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LockButtonWidget.class)
public abstract class LockIconButtonMixin {
   @Shadow
   private boolean locked;

   public LockIconButtonMixin() {
   }

   @Inject(method = "drawIcon", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$glassLock(DrawContext g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      if (GlassWidgets.renderLock((LockButtonWidget)(Object)this, g, this.locked)) {
         ci.cancel();
      }
   }
}
