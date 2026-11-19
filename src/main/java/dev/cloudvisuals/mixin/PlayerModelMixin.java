package dev.cloudvisuals.mixin;

import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.visual.SelfViewModule;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerModelMixin {
   public PlayerModelMixin() {
   }

   @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
   private void cloudvisuals$selfView(PlayerEntityRenderState state, CallbackInfo ci) {
      PlayerEntityModel model = (PlayerEntityModel)(Object)this;
      SelfViewModule self = Modules.selfView();
      boolean hide = self != null && self.isSelf(state);
      model.head.visible = !hide;
      if (hide) {
         self.hideParts(model);
      }
   }
}
