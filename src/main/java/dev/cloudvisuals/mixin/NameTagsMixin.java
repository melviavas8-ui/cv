package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.visual.NametagsModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.LabelCommandRenderer;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LabelCommandRenderer.class)
public abstract class NameTagsMixin {
   public NameTagsMixin() {
   }

   @WrapOperation(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)V"
      )
   )
   private void cloudvisuals$nametagStyle(
      TextRenderer font,
      Text text,
      float x,
      float y,
      int color,
      boolean shadow,
      Matrix4f matrix,
      VertexConsumerProvider buffers,
      TextLayerType mode,
      int background,
      int light,
      Operation<Void> original
   ) {
      NametagsModule module = Modules.nametags();
      if (module != null && module.isEnabled()) {
         int newBackground = background != 0 ? module.background(background) : 0;
         boolean newShadow = background == 0 && module.shadow(shadow);
         original.call(new Object[]{font, text, x, y, color, newShadow, matrix, buffers, mode, newBackground, light});
      } else {
         original.call(new Object[]{font, text, x, y, color, shadow, matrix, buffers, mode, background, light});
      }
   }
}
