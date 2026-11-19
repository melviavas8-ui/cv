package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.cloudvisuals.gui.PromoRecorder;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.render.GlowEngine;
import dev.cloudvisuals.render.PostEffects;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
   public GameRendererMixin() {
   }

   @Inject(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/GameRenderer;renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V",
         shift = Shift.AFTER
      )
   )
   private void cloudvisuals$postEffects(RenderTickCounter deltaTracker, boolean renderLevel, CallbackInfo ci) {
      PostEffects.apply();
   }

   @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
   private float cloudvisuals$zoom(float fov, @Local(argsOnly = true) boolean useFovSetting) {
      return useFovSetting && Modules.zoom() != null ? fov / Modules.zoom().factor() : fov;
   }

   @ModifyArg(method = "getBasicProjectionMatrix", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;perspective(FFFF)Lorg/joml/Matrix4f;"), index = 1)
   private float cloudvisuals$aspect(float aspect) {
      return Modules.aspectRatio() != null ? Modules.aspectRatio().aspect(aspect) : aspect;
   }

   @Inject(method = "render", at = @At("RETURN"), require = 0)
   private void cloudvisuals$promoFrame(RenderTickCounter tracker, boolean renderLevel, CallbackInfo ci) {
      PromoRecorder.frame();
   }

   @Inject(method = "renderHand", at = @At("HEAD"))
   private void cloudvisuals$handGlowStart(float partialTick, boolean sleeping, Matrix4f view, CallbackInfo ci) {
      GlowEngine.beginWorld();
   }

   @Inject(
      method = "renderWorld",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFog(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V")
   )
   private void cloudvisuals$handGlowEnd(RenderTickCounter deltaTracker, CallbackInfo ci) {
      GlowEngine.endWorld();
   }

   @ModifyExpressionValue(
      method = "tiltViewWhenHurt",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;")
   )
   private Object cloudvisuals$hurtTilt(Object strength) {
      return strength instanceof Double value && Modules.camera() != null ? Modules.camera().hurtTilt(value) : strength;
   }

   @ModifyExpressionValue(method = "bobView", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerLikeState;lerpMovement(F)F"))
   private float cloudvisuals$bobbing(float bob) {
      return Modules.camera() != null ? Modules.camera().bobbing(bob) : bob;
   }

   @ModifyExpressionValue(
      method = "getFov",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 0)
   )
   private Object cloudvisuals$fov(Object fov) {
      return fov instanceof Integer value && Modules.camera() != null ? Modules.camera().fov(value) : fov;
   }

   @ModifyExpressionValue(method = "getFov", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
   private float cloudvisuals$fovModifier(float modifier) {
      return Modules.camera() != null ? Modules.camera().fovModifier(modifier) : modifier;
   }
}
