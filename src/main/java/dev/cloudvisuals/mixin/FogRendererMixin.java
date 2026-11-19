package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.world.SkyModule;
import java.nio.ByteBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
   public FogRendererMixin() {
   }

   @ModifyReturnValue(method = "getFogColor", at = @At("RETURN"))
   private Vector4f cloudvisuals$fogColor(Vector4f color, @Local(argsOnly = true) Camera camera) {
      SkyModule sky = Modules.sky();
      if (sky != null && sky.atmosphere(camera)) {
         float[] rgb = new float[]{color.x, color.y, color.z};
         sky.fogColor(rgb);
         return new Vector4f(rgb[0], rgb[1], rgb[2], color.w);
      } else {
         return color;
      }
   }

   @WrapOperation(
      method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/fog/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V")
   )
   private void cloudvisuals$fogDistance(
      FogRenderer self,
      ByteBuffer buffer,
      int offset,
      Vector4f color,
      float environmentalStart,
      float environmentalEnd,
      float renderDistanceStart,
      float renderDistanceEnd,
      float skyEnd,
      float cloudEnd,
      Operation<Void> original,
      @Local(argsOnly = true) Camera camera
   ) {
      SkyModule sky = Modules.sky();
      float scale = sky != null && sky.atmosphere(camera) ? sky.fogDistance() : 1.0F;
      if (scale == 1.0F) {
         original.call(
            new Object[]{self, buffer, offset, color, environmentalStart, environmentalEnd, renderDistanceStart, renderDistanceEnd, skyEnd, cloudEnd}
         );
      } else {
         original.call(
            new Object[]{
               self,
               buffer,
               offset,
               color,
               environmentalStart * scale,
               environmentalEnd * scale,
               renderDistanceStart * scale,
               renderDistanceEnd * scale,
               skyEnd,
               cloudEnd
            }
         );
      }
   }
}
