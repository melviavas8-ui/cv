package dev.cloudvisuals.render.world;

import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.cosmetic.AfterimageModule;
import dev.cloudvisuals.module.impl.cosmetic.AuraModule;
import dev.cloudvisuals.module.impl.cosmetic.JumpCirclesModule;
import dev.cloudvisuals.module.impl.cosmetic.KillEffectsModule;
import dev.cloudvisuals.module.impl.cosmetic.PetModule;
import dev.cloudvisuals.module.impl.cosmetic.TrailModule;
import dev.cloudvisuals.module.impl.visual.HitEffectsModule;
import dev.cloudvisuals.module.impl.visual.TargetEspModule;
import dev.cloudvisuals.module.impl.world.AmbientParticlesModule;
import dev.cloudvisuals.module.impl.world.WaypointsModule;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.world.life.Motion;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;

public final class WorldCosmetics {
   private static long lastFrame;

   private WorldCosmetics() {
   }

   public static void render(WorldRenderContext context) {
      MinecraftClient mc = MinecraftClient.getInstance();
      ClientPlayerEntity player = mc.player;
      MatrixStack pose = context.matrices();
      if (player != null && mc.world != null && pose != null) {
         AuraModule aura = Modules.aura();
         TrailModule trail = Modules.trail();
         JumpCirclesModule circles = Modules.jumpCircles();
         PetModule pet = Modules.pet();
         KillEffectsModule kills = Modules.killEffects();
         AmbientParticlesModule particles = Modules.ambientParticles();
         HitEffectsModule hits = Modules.hitEffects();
         AfterimageModule echoes = Modules.afterimage();
         WaypointsModule waypoints = Modules.waypoints();
         TargetEspModule targetEsp = Modules.targetEsp();
         boolean any = aura != null && aura.isEnabled()
            || trail != null && trail.isEnabled()
            || circles != null && circles.isEnabled()
            || pet != null && pet.isEnabled()
            || kills != null && kills.isEnabled()
            || particles != null && particles.isEnabled()
            || hits != null && hits.isEnabled() && hits.active()
            || echoes != null && echoes.isEnabled() && echoes.active()
            || waypoints != null && waypoints.isEnabled() && waypoints.active()
            || targetEsp != null && targetEsp.isEnabled();
         long now = System.nanoTime();
         float delta = lastFrame == 0L ? 0.0F : Math.min(0.1F, (float)(now - lastFrame) / 1.0E9F);
         lastFrame = now;
         Motion.update(player, delta);
         if (any) {
            Camera camera = context.gameRenderer().getCamera();
            Vector3f right = new Vector3f(camera.getDiagonalPlane()).negate();
            Vector3f up = new Vector3f(camera.getVerticalPlane());
            CosmeticContext ctx = new CosmeticContext(
               pose,
               mc.getBufferBuilders().getEntityVertexConsumers(),
               camera,
               camera.getCameraPos(),
               player,
               mc.getRenderTickCounter().getTickProgress(false),
               Theme.time(),
               delta,
               mc.options.getPerspective().isFirstPerson(),
               right,
               up
            );
            if (particles != null && particles.isEnabled()) {
               Modules.guard(particles, () -> particles.render(ctx));
            }

            if (kills != null && kills.isEnabled()) {
               Modules.guard(kills, () -> kills.render(ctx));
            }

            if (hits != null && hits.isEnabled() && hits.active()) {
               Modules.guard(hits, () -> hits.render(ctx));
            }

            if (!player.isInvisible()) {
               if (echoes != null && echoes.isEnabled() && echoes.active()) {
                  Modules.guard(echoes, () -> echoes.render(ctx));
               }

               if (circles != null && circles.isEnabled()) {
                  Modules.guard(circles, () -> circles.render(ctx));
               }

               if (aura != null && aura.isEnabled()) {
                  Modules.guard(aura, () -> aura.render(ctx));
               }

               if (trail != null && trail.isEnabled()) {
                  Modules.guard(trail, () -> trail.render(ctx));
               }

               if (pet != null && pet.isEnabled()) {
                  Modules.guard(pet, () -> pet.render(ctx));
               }
            }

            if (waypoints != null && waypoints.isEnabled() && waypoints.active()) {
               Modules.guard(waypoints, () -> waypoints.render(ctx));
            }

            if (targetEsp != null && targetEsp.isEnabled()) {
               Modules.guard(targetEsp, () -> targetEsp.render(ctx));
            }

            ctx.buffers().draw();
         }
      }
   }
}
