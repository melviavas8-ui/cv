package dev.cloudvisuals.render.world;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public record CosmeticContext(
   MatrixStack pose,
   Immediate buffers,
   Camera camera,
   Vec3d cameraPos,
   ClientPlayerEntity player,
   float partialTick,
   float time,
   float delta,
   boolean firstPerson,
   Vector3f right,
   Vector3f up
) {
   public CosmeticContext {
   }

   public Vector3f local(double x, double y, double z) {
      return new Vector3f((float)(x - this.cameraPos.x), (float)(y - this.cameraPos.y), (float)(z - this.cameraPos.z));
   }

   public Vector3f local(Vec3d world) {
      return this.local(world.x, world.y, world.z);
   }

   public static float nearFade(Vector3f cameraRelative, float near, float far) {
      return Math.clamp((cameraRelative.length() - near) / (far - near), 0.0F, 1.0F);
   }
}
