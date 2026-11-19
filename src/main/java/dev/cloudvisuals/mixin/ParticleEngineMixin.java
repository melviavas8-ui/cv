package dev.cloudvisuals.mixin;

import dev.cloudvisuals.module.Modules;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleEngineMixin {
   public ParticleEngineMixin() {
   }

   @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$density(Particle particle, CallbackInfo ci) {
      if (Modules.particles() != null && Modules.particles().skip()) {
         ci.cancel();
      }
   }
}
