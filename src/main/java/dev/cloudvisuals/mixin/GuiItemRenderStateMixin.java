package dev.cloudvisuals.mixin;

import dev.cloudvisuals.render.gpu.ItemAlpha;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemGuiElementRenderState.class)
public abstract class GuiItemRenderStateMixin implements ItemAlpha.Holder {
   @Unique
   private float cloudvisuals$alpha = 1.0F;

   public GuiItemRenderStateMixin() {
   }

   @Inject(method = "<init>", at = @At("TAIL"))
   private void cloudvisuals$rememberAlpha(CallbackInfo ci) {
      this.cloudvisuals$alpha = ItemAlpha.current;
   }

   @Override
   public float cloudvisuals$alpha() {
      return this.cloudvisuals$alpha;
   }
}
