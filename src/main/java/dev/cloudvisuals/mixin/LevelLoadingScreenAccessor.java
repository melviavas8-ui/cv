package dev.cloudvisuals.mixin;

import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.world.ClientChunkLoadProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelLoadingScreen.class)
public interface LevelLoadingScreenAccessor {
   @Accessor("chunkLoadProgress")
   ClientChunkLoadProgress cloudvisuals$loadTracker();
}
