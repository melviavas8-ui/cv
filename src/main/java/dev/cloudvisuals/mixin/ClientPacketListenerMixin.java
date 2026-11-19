package dev.cloudvisuals.mixin;

import dev.cloudvisuals.combat.CombatEvents;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPacketListenerMixin {
   @Shadow
   private ClientWorld world;

   public ClientPacketListenerMixin() {
   }

   @Inject(method = "onEntityDamage", at = @At("TAIL"))
   private void cloudvisuals$damage(EntityDamageS2CPacket packet, CallbackInfo ci) {
      if (this.world != null) {
         CombatEvents.onDamage(this.world.getEntityById(packet.entityId()), packet.sourceCauseId());
      }
   }

   @Inject(method = "onEntityAnimation", at = @At("TAIL"))
   private void cloudvisuals$animate(EntityAnimationS2CPacket packet, CallbackInfo ci) {
      int action = packet.getAnimationId();
      if (this.world != null && (action == 4 || action == 5)) {
         CombatEvents.onCrit(this.world.getEntityById(packet.getEntityId()));
      }
   }

   @Inject(method = "onEntityStatus", at = @At("TAIL"))
   private void cloudvisuals$entityEvent(EntityStatusS2CPacket packet, CallbackInfo ci) {
      if (this.world != null) {
         byte event = packet.getStatus();
         if (event == 3) {
            CombatEvents.onDeath(packet.getEntity(this.world));
         } else if (event == 35) {
            CombatEvents.onTotem(packet.getEntity(this.world));
         }
      }
   }
}
