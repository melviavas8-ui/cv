package dev.cloudvisuals.render.world.life;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class Motion {
   private static final double TELEPORT = 64.0;
   private static Vec3d position = Vec3d.ZERO;
   private static Vec3d velocity = Vec3d.ZERO;
   private static Vec3d acceleration = Vec3d.ZERO;
   private static float speed;
   private static float horizontal;
   private static float fall;
   private static float yawRate;
   private static float pitchRate;
   private static float lastYaw;
   private static float lastPitch;
   private static float airborne;
   private static float sinceLanding;
   private static boolean ready;

   private Motion() {
   }

   public static void update(ClientPlayerEntity player, float dt) {
      if (player == null) {
         ready = false;
      } else {
         Vec3d now = player.getEntityPos();
         if (ready && !(dt <= 0.0F) && !(now.squaredDistanceTo(position) > 64.0)) {
            Vec3d previous = velocity;
            velocity = smooth(velocity, now.subtract(position).multiply(1.0F / dt), dt, 0.08F);
            acceleration = smooth(acceleration, velocity.subtract(previous).multiply(1.0F / dt), dt, 0.12F);
            position = now;
            speed = (float)velocity.length();
            horizontal = (float)Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            fall = (float)(-velocity.y);
            float yaw = player.getYaw();
            float pitch = player.getPitch();
            yawRate = smooth(yawRate, MathHelper.subtractAngles(lastYaw, yaw) / dt, dt, 0.06F);
            pitchRate = smooth(pitchRate, (pitch - lastPitch) / dt, dt, 0.06F);
            lastYaw = yaw;
            lastPitch = pitch;
            if (player.isOnGround()) {
               airborne = 0.0F;
               sinceLanding += dt;
            } else {
               airborne += dt;
               sinceLanding = 0.0F;
            }
         } else {
            restart(player, now);
         }
      }
   }

   private static void restart(ClientPlayerEntity player, Vec3d now) {
      position = now;
      velocity = acceleration = Vec3d.ZERO;
      pitchRate = 0.0F;
      yawRate = 0.0F;
      fall = 0.0F;
      horizontal = 0.0F;
      speed = 0.0F;
      lastYaw = player.getYaw();
      lastPitch = player.getPitch();
      sinceLanding = 0.0F;
      airborne = 0.0F;
      ready = true;
   }

   public static Vec3d velocity() {
      return velocity;
   }

   public static Vec3d acceleration() {
      return acceleration;
   }

   public static float speed() {
      return speed;
   }

   public static float horizontalSpeed() {
      return horizontal;
   }

   public static float fallSpeed() {
      return fall;
   }

   public static float yawRate() {
      return yawRate;
   }

   public static float pitchRate() {
      return pitchRate;
   }

   public static float airborne() {
      return airborne;
   }

   public static float sinceLanding() {
      return sinceLanding;
   }

   public static float intensity() {
      return Math.clamp(speed / 12.0F, 0.0F, 1.0F);
   }

   private static Vec3d smooth(Vec3d current, Vec3d target, float dt, float tau) {
      double a = rate(dt, tau);
      return current.add(target.subtract(current).multiply(a));
   }

   private static float smooth(float current, float target, float dt, float tau) {
      return current + (target - current) * rate(dt, tau);
   }

   private static float rate(float dt, float tau) {
      return 1.0F - (float)Math.exp(-dt / tau);
   }
}
