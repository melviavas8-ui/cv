package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import dev.cloudvisuals.compat.Compat;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.visual.SelfViewModule;
import dev.cloudvisuals.module.impl.world.ChunkFadeModule;
import dev.cloudvisuals.module.impl.world.SkyModule;
import dev.cloudvisuals.render.world.WorldProjection;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.memory.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class LevelRendererMixin {
   public LevelRendererMixin() {
   }

   @Inject(method = "render", at = @At("HEAD"))
   private void cloudvisuals$rememberProjection(
      ObjectAllocator allocator,
      RenderTickCounter deltaTracker,
      boolean renderOutline,
      Camera camera,
      Matrix4f view,
      Matrix4f projection,
      Matrix4f cullProjection,
      GpuBufferSlice fog,
      Vector4f fogColor,
      boolean renderSky,
      CallbackInfo ci
   ) {
      WorldProjection.set(projection, view);
   }

   @ModifyExpressionValue(method = "fillEntityRenderStates", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;isThirdPerson()Z"))
   private boolean cloudvisuals$renderSelf(boolean detached) {
      SelfViewModule self = Modules.selfView();
      return detached || self != null && self.showsSelf();
   }

   @ModifyExpressionValue(
      method = "updateChunks",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 0)
   )
   private Object cloudvisuals$fadeDuration(Object seconds) {
      ChunkFadeModule fade = Modules.chunkFade();
      return fade != null && fade.active() && !Compat.shaderPackActive() ? fade.totalSeconds() : seconds;
   }

   @ModifyExpressionValue(
      method = "renderBlockLayers",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;method_76298(J)F")
   )
   private float cloudvisuals$fadeStyle(float visibility) {
      ChunkFadeModule fade = Modules.chunkFade();
      return fade != null && fade.active() && !Compat.shaderPackActive() ? fade.encode(visibility) : visibility;
   }

   @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$clouds(CallbackInfo ci) {
      SkyModule sky = Modules.sky();
      if (sky != null && sky.hideClouds()) {
         ci.cancel();
      }
   }

   @ModifyArg(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/WorldRenderer;renderClouds(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/option/CloudRenderMode;Lnet/minecraft/util/math/Vec3d;JFIF)V"
      ),
      index = 5
   )
   private int cloudvisuals$cloudColor(int color) {
      SkyModule sky = Modules.sky();
      return sky != null && !Compat.shaderPackActive() ? sky.cloudColor(color) : color;
   }
}
