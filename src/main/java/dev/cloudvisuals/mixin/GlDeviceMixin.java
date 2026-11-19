package dev.cloudvisuals.mixin;

import dev.cloudvisuals.render.gpu.ColorTarget;
import net.minecraft.client.gl.GlBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GlBackend.class)
public abstract class GlDeviceMixin {
   public GlDeviceMixin() {
   }

   @ModifyArg(
      method = "createTexture(Ljava/lang/String;ILcom/mojang/blaze3d/textures/TextureFormat;IIII)Lcom/mojang/blaze3d/textures/GpuTexture;",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_texImage2D(IIIIIIIILjava/nio/ByteBuffer;)V"),
      index = 2
   )
   private int cloudvisuals$floatFormat(int internalFormat) {
      return ColorTarget.floatRequested ? 34842 : internalFormat;
   }
}
