package dev.cloudvisuals.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.util.ColorUtil;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderLayer.class)
public abstract class RenderTypeMixin {
   public RenderTypeMixin() {
   }

   @Shadow
   public abstract RenderPipeline getRenderPipeline();

   @ModifyArg(
      method = "draw",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gl/DynamicUniforms;write(Lorg/joml/Matrix4fc;Lorg/joml/Vector4fc;Lorg/joml/Vector3fc;Lorg/joml/Matrix4fc;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
      ),
      index = 1
   )
   private Vector4fc cloudvisuals$glintColor(Vector4fc color) {
      if (this.getRenderPipeline() != RenderPipelines.GLINT) {
         return color;
      }

      int tint = Modules.entityColors() != null ? Modules.entityColors().glintColor() : 0;
      return (Vector4fc)(tint == 0 ? color : new Vector4f(ColorUtil.red(tint) / 255.0F, ColorUtil.green(tint) / 255.0F, ColorUtil.blue(tint) / 255.0F, 1.0F));
   }
}
