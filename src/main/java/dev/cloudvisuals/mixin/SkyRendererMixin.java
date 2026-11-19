package dev.cloudvisuals.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.cloudvisuals.compat.Compat;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.world.SkyModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.render.state.SkyRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.MoonPhase;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRendering.class)
public abstract class SkyRendererMixin {
   @Unique
   private float cloudvisuals$vanillaStars;

   public SkyRendererMixin() {
   }

   @Inject(method = "updateRenderState", at = @At("TAIL"))
   private void cloudvisuals$skyColor(ClientWorld level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo ci) {
      SkyModule sky = Modules.sky();
      if (sky != null && !Compat.shaderPackActive()) {
         state.skyColor = sky.skyColor(state.skyColor);
      }
   }

   @ModifyVariable(method = "renderCelestialBodies", at = @At("HEAD"), argsOnly = true, ordinal = 4)
   private float cloudvisuals$stars(float brightness) {
      this.cloudvisuals$vanillaStars = brightness;
      SkyModule sky = Modules.sky();
      return sky != null && !Compat.shaderPackActive() ? sky.starBrightness(brightness) : brightness;
   }

   @Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$sun(float rainBrightness, MatrixStack pose, CallbackInfo ci) {
      SkyModule sky = Modules.sky();
      if (sky != null && sky.hideSun()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$moon(MoonPhase phase, float rainBrightness, MatrixStack pose, CallbackInfo ci) {
      SkyModule sky = Modules.sky();
      if (sky != null && sky.hideMoon()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderCelestialBodies", at = @At("TAIL"))
   private void cloudvisuals$overlay(
      MatrixStack pose, float sunAngle, float moonAngle, float starAngle, MoonPhase phase, float rainBrightness, float starBrightness, CallbackInfo ci
   ) {
      SkyModule sky = Modules.sky();
      if (sky != null && sky.isEnabled() && !Compat.shaderPackActive()) {
         Matrix4f rotation = new Matrix4f(RenderSystem.getModelViewMatrix());
         Modules.guard(sky, () -> sky.renderOverlay(rotation, sunAngle, starAngle, rainBrightness, this.cloudvisuals$vanillaStars));
      }
   }
}
