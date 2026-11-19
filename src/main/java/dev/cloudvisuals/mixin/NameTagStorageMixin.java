package dev.cloudvisuals.mixin;

import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.visual.NametagsModule;
import net.minecraft.client.render.command.LabelCommandRenderer.Commands;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public abstract class NameTagStorageMixin {
   public NameTagStorageMixin() {
   }

   @Inject(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", shift = Shift.AFTER))
   private void cloudvisuals$nametagScale(
      MatrixStack pose, Vec3d attachment, int offset, Text name, boolean discrete, int light, double distance, CameraRenderState camera, CallbackInfo ci
   ) {
      NametagsModule module = Modules.nametags();
      if (module != null) {
         float scale = module.scale();
         if (scale != 1.0F) {
            pose.scale(scale, scale, scale);
         }
      }
   }
}
