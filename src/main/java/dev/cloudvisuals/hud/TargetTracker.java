package dev.cloudvisuals.hud;

import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.visual.HitEffectsModule;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

public final class TargetTracker {
   private static LivingEntity attacked;
   private static long attackedAt;

   private TargetTracker() {
   }

   public static void register() {
      AttackEntityCallback.EVENT.register((AttackEntityCallback)(player, level, hand, entity, hitResult) -> {
         if (level.isClient() && entity instanceof LivingEntity living) {
            attacked = living;
            attackedAt = System.currentTimeMillis();
            HitEffectsModule effects = Modules.hitEffects();
            if (effects != null && effects.isEnabled()) {
               effects.onClick(living);
            }
         }

         return ActionResult.PASS;
      });
   }

   public static long lastAttackMillis() {
      return attackedAt;
   }

   public static LivingEntity target(long keepMillis, boolean crosshair) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world == null) {
         attacked = null;
         return null;
      }

      if (attacked != null && (attacked.isRemoved() || attacked.getEntityWorld() != mc.world || System.currentTimeMillis() - attackedAt > keepMillis)) {
         attacked = null;
      }

      if (attacked != null) {
         return attacked.isInvisibleTo(mc.player) ? null : attacked;
      } else {
         return crosshair && mc.targetedEntity instanceof LivingEntity living && living.isAlive() && !living.isInvisibleTo(mc.player) ? living : null;
      }
   }
}
