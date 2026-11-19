package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.cosmetic.ItemGlowModule;
import dev.cloudvisuals.module.impl.cosmetic.SwingTrailModule;
import dev.cloudvisuals.module.impl.visual.HandModule;
import dev.cloudvisuals.render.world.ItemProbe;
import dev.cloudvisuals.render.world.RecordingBuffers;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class ItemInHandRendererMixin {
   @Unique
   private final ItemProbe cloudvisuals$probe = new ItemProbe();
   @Unique
   private final RecordingBuffers cloudvisuals$buffers = new RecordingBuffers();

   public ItemInHandRendererMixin() {
   }

   @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = Shift.AFTER))
   private void cloudvisuals$handTransform(
      AbstractClientPlayerEntity player,
      float partialTick,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack stack,
      float equipProgress,
      MatrixStack pose,
      OrderedRenderCommandQueue collector,
      int light,
      CallbackInfo ci
   ) {
      HandModule module = Modules.hand();
      if (module != null) {
         module.applyHandTransform(pose, player, hand, stack);
      }
   }

   @ModifyVariable(method = "renderFirstPersonItem", at = @At("HEAD"), argsOnly = true, ordinal = 2)
   private float cloudvisuals$swingProgress(float swingProgress, @Local(argsOnly = true) Hand hand, @Local(argsOnly = true) ItemStack stack) {
      HandModule module = Modules.hand();
      return module != null ? module.swingProgress(hand, stack, swingProgress) : swingProgress;
   }

   @ModifyVariable(method = "renderFirstPersonItem", at = @At("HEAD"), argsOnly = true, ordinal = 3)
   private float cloudvisuals$equipProgress(float equipProgress) {
      HandModule module = Modules.hand();
      return module != null ? module.equipProgress(equipProgress) : equipProgress;
   }

   @WrapOperation(
      method = "swingArm",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applySwingOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V"
      )
   )
   private void cloudvisuals$attackRotation(HeldItemRenderer renderer, MatrixStack pose, Arm arm, float progress, Operation<Void> original) {
      HandModule module = Modules.hand();
      original.call(new Object[]{renderer, pose, arm, module != null && module.skipAttackRotation() ? 0.0F : progress});
   }

   @WrapOperation(
      method = "renderFirstPersonItem",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V"
      )
   )
   private void cloudvisuals$itemTransform(
      HeldItemRenderer renderer,
      LivingEntity entity,
      ItemStack stack,
      ItemDisplayContext context,
      MatrixStack pose,
      OrderedRenderCommandQueue collector,
      int light,
      Operation<Void> original
   ) {
      boolean leftHand = context.isLeftHand();
      HandModule module = Modules.hand();
      boolean transform = module != null && module.isEnabled();
      if (transform) {
         pose.push();
         module.applyItemTransform(pose, entity, leftHand);
      }

      original.call(new Object[]{renderer, entity, stack, context, pose, collector, light});
      if (context.isFirstPerson() && entity instanceof ClientPlayerEntity player) {
         boolean mainHand = leftHand == (player.getMainArm() == Arm.LEFT);
         ItemGlowModule glow = Modules.itemGlow();
         if (glow != null && glow.wants(stack, mainHand)) {
            Modules.guard(glow, () -> original.call(new Object[]{renderer, entity, stack, context, pose, glow.silhouette(collector, mainHand), light}));
         }

         SwingTrailModule trail = Modules.swingTrail();
         if (trail != null && trail.isEnabled()) {
            Modules.guard(trail, () -> {
               original.call(new Object[]{renderer, entity, stack, context, pose, this.cloudvisuals$probe.capture(), light});
               trail.afterItem(this.cloudvisuals$probe, entity, stack, context, leftHand, this.cloudvisuals$buffers);
               this.cloudvisuals$buffers.submit(collector);
            });
         }
      }

      if (transform) {
         pose.pop();
      }
   }
}
