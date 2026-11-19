package dev.cloudvisuals.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.cloudvisuals.render.gpu.GpuFrame;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.tracy.TracyFrameCapturer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {
   public RenderSystemMixin() {
   }

   @Inject(method = "flipFrame", at = @At("TAIL"))
   private static void cloudvisuals$endFrame(Window window, @Nullable TracyFrameCapturer capture, CallbackInfo ci) {
      GpuFrame.end();
   }
}
