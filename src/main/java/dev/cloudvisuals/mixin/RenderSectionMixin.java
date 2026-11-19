package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.cloudvisuals.compat.Compat;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.world.ChunkFadeModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BuiltChunk.class)
public abstract class RenderSectionMixin {
   public RenderSectionMixin() {
   }

   @Shadow
   public abstract long getSectionPos();

   @ModifyReturnValue(method = "method_76298", at = @At("RETURN"))
   private float cloudvisuals$visibility(float vanilla) {
      if (vanilla >= 1.0F) {
         return vanilla;
      } else {
         ChunkFadeModule fade = Modules.chunkFade();
         if (fade != null && fade.active() && !Compat.shaderPackActive()) {
            Vec3d eye = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
            long node = this.getSectionPos();
            double dx = ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackX(node), 8) - eye.x;
            double dy = ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackY(node), 8) - eye.y;
            double dz = ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackZ(node), 8) - eye.z;
            return fade.visibility(vanilla, dx * dx + dy * dy + dz * dz);
         } else {
            return vanilla;
         }
      }
   }
}
