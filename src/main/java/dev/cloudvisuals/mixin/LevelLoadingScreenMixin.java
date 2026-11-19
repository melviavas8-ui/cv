package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.cloudvisuals.gui.screen.StatusDesign;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.world.chunk.ChunkLoadMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin {
   public LevelLoadingScreenMixin() {
   }

   @WrapWithCondition(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/screen/world/LevelLoadingScreen;drawChunkMap(Lnet/minecraft/client/gui/DrawContext;IIIILnet/minecraft/world/chunk/ChunkLoadMap;)V"
      )
   )
   private boolean cloudvisuals$chunks(DrawContext g, int x, int y, int size, int spacing, ChunkLoadMap view) {
      return !StatusDesign.styled((LevelLoadingScreen)(Object)this);
   }

   @WrapWithCondition(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/screen/world/LevelLoadingScreen;drawLoadingBar(Lnet/minecraft/client/gui/DrawContext;IIIIF)V"
      )
   )
   private boolean cloudvisuals$progressBar(LevelLoadingScreen screen, DrawContext g, int x, int y, int width, int height, float progress) {
      return !StatusDesign.styled(screen);
   }
}
