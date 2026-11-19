package dev.cloudvisuals.mixin;

import dev.cloudvisuals.module.Modules;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientLevelParticlesMixin {
   public ClientLevelParticlesMixin() {
   }

   @Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$blockBreak(BlockPos pos, BlockState state, CallbackInfo ci) {
      if (Modules.particles() != null && Modules.particles().hideBlockBreak()) {
         ci.cancel();
      }
   }

   @Inject(method = "spawnBlockBreakingParticle", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$blockCrack(BlockPos pos, Direction side, CallbackInfo ci) {
      if (Modules.particles() != null && Modules.particles().hideBlockCrack()) {
         ci.cancel();
      }
   }
}
