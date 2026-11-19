package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.world.ItemPhysicsModule;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {
   public ItemEntityRendererMixin() {
   }

   @Inject(method = "updateRenderState(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;F)V", at = @At("TAIL"))
   private void cloudvisuals$remember(ItemEntity item, ItemEntityRenderState state, float partialTick, CallbackInfo ci) {
      ((ItemPhysicsModule.State)state).cloudvisuals$physics(item.isOnGround(), item.getId(), item.getStack().getItem() instanceof BlockItem);
   }

   @WrapOperation(
      method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0)
   )
   private void cloudvisuals$lift(MatrixStack pose, float x, float y, float z, Operation<Void> original, @Local(argsOnly = true) ItemEntityRenderState state) {
      ItemPhysicsModule physics = Modules.itemPhysics();
      if (physics != null && physics.isEnabled()) {
         float rest = -((float)state.itemRenderState.getModelBoundingBox().minY) + 0.0625F;
         original.call(new Object[]{pose, 0.0F, physics.lift((ItemPhysicsModule.State)state, rest), 0.0F});
      } else {
         original.call(new Object[]{pose, x, y, z});
      }
   }

   @WrapOperation(
      method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionfc;)V", ordinal = 0)
   )
   private void cloudvisuals$rotate(MatrixStack pose, Quaternionfc rotation, Operation<Void> original, @Local(argsOnly = true) ItemEntityRenderState state) {
      ItemPhysicsModule physics = Modules.itemPhysics();
      if (physics != null && physics.isEnabled()) {
         physics.rotate(pose, (ItemPhysicsModule.State)state, state.age);
      } else {
         original.call(new Object[]{pose, rotation});
      }
   }
}
