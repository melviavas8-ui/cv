package dev.cloudvisuals.module.impl.cosmetic;

import dev.cloudvisuals.render.CloudVisualsRenderTypes;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.world.CosmeticContext;
import dev.cloudvisuals.render.world.CosmeticTextures;
import dev.cloudvisuals.render.world.Geometry;
import dev.cloudvisuals.render.world.VoxelPets;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.ChoiceSetting;
import dev.cloudvisuals.setting.NumberSetting;
import dev.cloudvisuals.util.ColorUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public final class PetModule extends CosmeticModule {
   private final ChoiceSetting<PetModule.Pet> pet = this.add(new ChoiceSetting<>("pet", PetModule.Pet.KING));
   private final ChoiceSetting<PetModule.Side> side = this.add(new ChoiceSetting<>("side", PetModule.Side.RIGHT));
   private final NumberSetting size = this.add(new NumberSetting("size", 100.0, 40.0, 250.0, 5.0, "cloudvisuals.unit.percent"));
   private final NumberSetting distance;
   private final NumberSetting height;
   private final NumberSetting bob;
   private final NumberSetting follow;
   private final BoolSetting firstPerson;
   private final BoolSetting particles;
   private final NumberSetting glow;
   private static final double WIND = 4.5;
   private Vec3d position;
   private Vec3d velocity;
   private final Deque<float[]> dust;
   private float dustBudget;
   private float facing;
   private float cheerAt;
   private final Vector3f wind;

   public PetModule() {
      super("pet");
      this.section("motion", 57776);
      this.distance = this.add(new NumberSetting("distance", 100.0, 50.0, 250.0, 5.0, "cloudvisuals.unit.percent"));
      this.height = this.add(new NumberSetting("height", 0.0, -60.0, 80.0, 1.0, ""));
      this.bob = this.add(new NumberSetting("bob", 100.0, 0.0, 300.0, 5.0, "cloudvisuals.unit.percent"));
      this.follow = this.add(new NumberSetting("follow", 100.0, 20.0, 300.0, 5.0, "cloudvisuals.unit.percent"));
      this.firstPerson = this.add(new BoolSetting("first_person", true));
      this.particles = this.add(new BoolSetting("particles", true));
      this.glow = this.add(new NumberSetting("glow", 70.0, 0.0, 100.0, 5.0, "cloudvisuals.unit.percent"));
      this.velocity = Vec3d.ZERO;
      this.dust = new ArrayDeque<>();
      this.cheerAt = -10.0F;
      this.wind = new Vector3f();
   }

   @Override
   public int icon() {
      return 57922;
   }

   @Override
   protected boolean alive() {
      return true;
   }

   @Override
   protected void onDisable() {
      this.position = null;
      this.velocity = Vec3d.ZERO;
      this.dust.clear();
   }

   public void cheer() {
      this.cheerAt = Theme.time();
   }

   public void render(CosmeticContext ctx) {
      MinecraftClient mc = MinecraftClient.getInstance();
      ClientPlayerEntity player = ctx.player();
      if (mc.world != null && (!ctx.firstPerson() || this.firstPerson.isOn())) {
         float time = ctx.time();
         float dt = Math.min(0.05F, ctx.delta());
         float sideSign = this.side.get() == PetModule.Side.RIGHT ? 1.0F : -1.0F;
         float reach = this.distance.floatValue() / 100.0F;
         float bobbing = (MathHelper.sin(time * 1.9F) * 0.07F + this.wobble(time, 2.0F, 0.05F)) * this.bob.floatValue() / 100.0F;
         Vec3d target;
         if (ctx.firstPerson()) {
            Vec3d eye = player.getCameraPosVec(ctx.partialTick());
            Vec3d look = player.getRotationVec(ctx.partialTick());
            Vec3d flat = new Vec3d(look.x, 0.0, look.z);
            if (flat.lengthSquared() < 1.0E-6) {
               flat = new Vec3d(0.0, 0.0, 1.0);
            }

            flat = flat.normalize();
            Vec3d right = flat.crossProduct(new Vec3d(0.0, 1.0, 0.0)).normalize();
            target = eye.add(flat.multiply(1.35 * reach))
               .add(right.multiply(0.95 * reach * sideSign))
               .add(0.0, -0.35 + this.height.floatValue() / 100.0F + bobbing, 0.0);
         } else {
            float bodyYaw = MathHelper.lerpAngleDegrees(ctx.partialTick(), player.lastBodyYaw, player.bodyYaw) * (float) (Math.PI / 180.0);
            double rightX = -Math.cos(bodyYaw);
            double rightZ = -Math.sin(bodyYaw);
            double backX = Math.sin(bodyYaw);
            double backZ = -Math.cos(bodyYaw);
            target = player.getLerpedPos(ctx.partialTick())
               .add(
                  rightX * 0.85 * reach * sideSign + backX * 0.35 * reach,
                  1.8 + this.height.floatValue() / 100.0F + bobbing,
                  rightZ * 0.85 * reach * sideSign + backZ * 0.35 * reach
               );
         }

         if (this.position == null || this.position.squaredDistanceTo(target) > 144.0) {
            this.position = target;
            this.velocity = Vec3d.ZERO;
         }

         float stiffness = 38.0F * this.follow.floatValue() / 100.0F;
         float damping = 2.0F * (float)Math.sqrt(stiffness) * 0.9F;
         Vec3d force = target.subtract(this.position).multiply(stiffness).subtract(this.velocity.multiply(damping));
         this.velocity = this.velocity.add(force.multiply(dt));
         if (this.life() > 0.0F) {
            this.flow(this.position.x, this.position.y, this.position.z, time, this.wind);
            this.velocity = this.velocity.add(this.wind.x * 4.5 * dt, this.wind.y * 4.5 * dt, this.wind.z * 4.5 * dt);
         }

         this.position = this.position.add(this.velocity.multiply(dt));
         Vec3d fromCamera = this.position.subtract(ctx.cameraPos());
         double minimum = 0.85 * reach;
         if (fromCamera.lengthSquared() < minimum * minimum && fromCamera.lengthSquared() > 1.0E-6) {
            this.position = ctx.cameraPos().add(fromCamera.normalize().multiply(minimum));
         }

         float scale = this.size.floatValue() / 100.0F;
         float strength = 0.35F + 0.65F * this.glow.floatValue() / 100.0F;
         Vector3f local = ctx.local(this.position);
         float nearFade = CosmeticContext.nearFade(local, 0.5F, 0.9F);
         if (!(nearFade <= 0.01F)) {
            if (this.particles.isOn()) {
               this.updateDust(dt, scale, time);
            }

            MatrixStack pose = ctx.pose();
            VertexConsumer sprites = ctx.buffers().getBuffer(CloudVisualsRenderTypes.glowSprite(CosmeticTextures.dot()));
            Geometry.dotSprite(sprites, pose.peek(), local, ctx.right(), ctx.up(), 0.38F * scale, this.color(0.0F, time, 0.45F * strength * nearFade));
            if (this.pet.get() == PetModule.Pet.WISP || this.pet.get() == PetModule.Pet.FAIRY) {
               Geometry.dotSprite(
                  sprites, pose.peek(), local, ctx.right(), ctx.up(), 0.12F * scale, ColorUtil.withAlpha(-1, Math.round(230.0F * nearFade * this.opacity()))
               );
            }

            for (float[] particle : this.dust) {
               Vector3f point = ctx.local(particle[0], particle[1], particle[2]);
               float fade = particle[3] * CosmeticContext.nearFade(point, 0.5F, 0.9F);
               Geometry.dotSprite(
                  sprites, pose.peek(), point, ctx.right(), ctx.up(), 0.035F * scale * (0.5F + particle[3]), this.color(particle[4], time, fade * strength)
               );
            }

            ctx.buffers().draw();
            int light = this.pet.get() == PetModule.Pet.CUBE ? WorldRenderer.getLightmapCoordinates(mc.world, BlockPos.ofFloored(this.position)) : 15728880;
            pose.push();
            pose.translate(local.x, local.y, local.z);
            pose.scale(scale, scale, scale);
            double yaw = this.velocity.horizontalLengthSquared() > 0.01 ? Math.atan2(this.velocity.x, this.velocity.z) : 0.0;
            float wanted = this.velocity.horizontalLengthSquared() > 0.5
               ? (float)Math.atan2(this.velocity.x, this.velocity.z)
               : -MathHelper.lerpAngleDegrees(ctx.partialTick(), player.lastBodyYaw, player.bodyYaw) * (float) (Math.PI / 180.0);
            this.facing = this.facing
               + MathHelper.wrapDegrees((wanted - this.facing) * (180.0F / (float)Math.PI)) * (float) (Math.PI / 180.0) * (1.0F - (float)Math.exp(-7.0F * dt));
            switch ((PetModule.Pet)this.pet.get()) {
               case KING:
                  this.voxel(ctx, pose, VoxelPets.Kind.KING, time, nearFade);
                  break;
               case CAT:
                  this.voxel(ctx, pose, VoxelPets.Kind.CAT, time, nearFade);
                  break;
               case DRAGON:
                  this.voxel(ctx, pose, VoxelPets.Kind.DRAGON, time, nearFade);
                  break;
               case GHOST:
                  this.voxel(ctx, pose, VoxelPets.Kind.GHOST, time, nearFade);
                  break;
               case BEE:
                  this.voxel(ctx, pose, VoxelPets.Kind.BEE, time, nearFade);
                  break;
               case SLIME:
                  this.voxel(ctx, pose, VoxelPets.Kind.SLIME, time, nearFade);
                  break;
               case FAIRY:
                  this.fairy(ctx, pose, time, (float)yaw, nearFade, strength);
                  break;
               case WISP:
                  this.wisp(ctx, pose, time, nearFade, strength);
                  break;
               case CRYSTAL:
                  this.crystal(ctx, pose, time, light, nearFade, strength);
                  break;
               case DROPLET:
                  this.droplet(ctx, pose, time, light, nearFade);
                  break;
               case CUBE:
                  this.cube(ctx, pose, time, light, nearFade, strength);
                  break;
               case STAR:
                  this.star(ctx, pose, time, light, nearFade, strength);
            }

            pose.pop();
            ctx.buffers().draw();
         }
      }
   }

   private void voxel(CosmeticContext ctx, MatrixStack pose, VoxelPets.Kind kind, float time, float nearFade) {
      MinecraftClient mc = MinecraftClient.getInstance();
      int packed = WorldRenderer.getLightmapCoordinates(mc.world, BlockPos.ofFloored(this.position));
      int block = Math.max(LightmapTextureManager.getBlockLightCoordinates(packed), 6 + Math.round(this.glow.floatValue() / 100.0F * 8.0F));
      int light = LightmapTextureManager.pack(Math.min(15, block), LightmapTextureManager.getSkyLightCoordinates(packed));
      pose.multiply(RotationAxis.POSITIVE_Y.rotation(this.facing));
      pose.translate(0.0F, -0.24F, 0.0F);
      float speed = (float)Math.clamp(this.velocity.length() / 4.0, 0.0, 1.0);
      float joy = Math.clamp(1.0F - (time - this.cheerAt) / 0.9F, 0.0F, 1.0F);
      VertexConsumer consumer = ctx.buffers().getBuffer(CloudVisualsRenderTypes.lit(CosmeticTextures.white()));
      VoxelPets.draw(kind, consumer, pose, light, time, speed, joy, this.primary(), this.secondary(), nearFade * this.opacity());
   }

   private void updateDust(float dt, float scale, float time) {
      this.dustBudget += dt * 14.0F;

      while (this.dustBudget >= 1.0F && this.dust.size() < 60) {
         this.dustBudget--;
         double angle = Math.random() * Math.PI * 2.0;
         this.dust
            .addLast(
               new float[]{
                  (float)(this.position.x + Math.cos(angle) * 0.08 * scale),
                  (float)(this.position.y - 0.05 * scale),
                  (float)(this.position.z + Math.sin(angle) * 0.08 * scale),
                  1.0F,
                  (float)Math.random()
               }
            );
      }

      if (this.dustBudget > 1.0F) {
         this.dustBudget = 1.0F;
      }

      Iterator<float[]> iterator = this.dust.iterator();

      while (iterator.hasNext()) {
         float[] particle = iterator.next();
         particle[3] -= dt / 0.9F;
         particle[1] -= dt * 0.25F;
         if (particle[3] <= 0.0F) {
            iterator.remove();
         }
      }
   }

   private int solid(float position, float time, float alpha, float lighten) {
      return ColorUtil.withAlpha(ColorUtil.lerp(this.rgb(position, time), -1, lighten), Math.clamp(Math.round(255.0F * alpha * this.opacity()), 0, 255));
   }

   private void fairy(CosmeticContext ctx, MatrixStack pose, float time, float yaw, float fade, float strength) {
      pose.push();
      pose.multiply(RotationAxis.POSITIVE_Y.rotation(yaw));
      VertexConsumer wings = ctx.buffers().getBuffer(CloudVisualsRenderTypes.lit(CosmeticTextures.butterfly()));
      float flap = MathHelper.sin(time * 38.0F) * 0.5F + 0.5F;

      for (int s = -1; s <= 1; s += 2) {
         for (int lobe = 0; lobe < 2; lobe++) {
            pose.push();
            pose.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(s * (15.0F + 55.0F * flap)));
            Entry last = pose.peek();
            float length = lobe == 0 ? 0.26F : 0.18F;
            float angle = lobe == 0 ? 25.0F : -20.0F;
            Vector3f root = new Vector3f(0.0F, 0.0F, 0.02F);
            Vector3f tip = new Vector3f(
               s * length * MathHelper.cos(angle * (float) (Math.PI / 180.0)), length * MathHelper.sin(angle * (float) (Math.PI / 180.0)), 0.06F
            );
            Vector3f across = new Vector3f(0.0F, lobe == 0 ? 0.07F : 0.05F, 0.0F);
            int c = this.solid(lobe * 0.3F, time, 0.55F * fade, 0.45F);
            Geometry.quadUv(
               wings,
               last,
               root,
               new Vector3f(tip).sub(across),
               new Vector3f(tip).add(across),
               new Vector3f(root).add(0.0F, 0.001F, 0.0F),
               0.0F,
               0.0F,
               1.0F,
               0.1F,
               1.0F,
               1.0F,
               0.0F,
               0.9F,
               c,
               c,
               c,
               c,
               15728880
            );
            pose.pop();
         }
      }

      pose.pop();
      VertexConsumer core = ctx.buffers().getBuffer(CloudVisualsRenderTypes.glowSprite(CosmeticTextures.dot()));
      Geometry.dotSprite(core, pose.peek(), new Vector3f(), rightOf(ctx), upOf(ctx), 0.2F, this.color(0.2F, time, 0.8F * fade * strength));
   }

   private void wisp(CosmeticContext ctx, MatrixStack pose, float time, float fade, float strength) {
      VertexConsumer flames = ctx.buffers().getBuffer(CloudVisualsRenderTypes.glowSprite(CosmeticTextures.dot()));
      Entry last = pose.peek();

      for (int i = 0; i < 14; i++) {
         float t = i / 13.0F;
         float flicker = MathHelper.sin(time * 9.0F + i * 1.7F) * 0.02F;
         Vector3f point = new Vector3f(flicker, t * 0.34F + MathHelper.sin(time * 3.0F + i) * 0.01F, -flicker);
         float size = 0.16F * (1.0F - t * 0.85F) * (0.85F + 0.15F * MathHelper.sin(time * 12.0F + i));
         Geometry.dotSprite(flames, last, point, rightOf(ctx), upOf(ctx), size, this.color(t * 0.7F, time, (1.0F - t) * 0.9F * fade * strength));
      }
   }

   private void crystal(CosmeticContext ctx, MatrixStack pose, float time, int light, float fade, float strength) {
      pose.push();
      pose.multiply(RotationAxis.POSITIVE_Y.rotation(time * 1.4F));
      pose.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(time * 1.1F) * 8.0F));
      Entry last = pose.peek();
      VertexConsumer consumer = ctx.buffers().getBuffer(CloudVisualsRenderTypes.lit(CosmeticTextures.white()));
      float r = 0.13F;
      float h = 0.22F;
      Vector3f top = new Vector3f(0.0F, h, 0.0F);
      Vector3f bottom = new Vector3f(0.0F, -h * 0.8F, 0.0F);
      int faces = 6;

      for (int i = 0; i < faces; i++) {
         double a0 = (Math.PI * 2) * i / faces;
         double a1 = (Math.PI * 2) * (i + 1) / faces;
         Vector3f p0 = new Vector3f((float)Math.cos(a0) * r, 0.0F, (float)Math.sin(a0) * r);
         Vector3f p1 = new Vector3f((float)Math.cos(a1) * r, 0.0F, (float)Math.sin(a1) * r);
         int bright = this.solid((float)i / faces, time, 0.82F * fade, i % 2 == 0 ? 0.35F : 0.1F);
         int dim = this.solid((float)i / faces + 0.5F, time, 0.82F * fade, i % 2 == 0 ? 0.05F : 0.2F);
         Geometry.triangle(consumer, last, p1, p0, top, bright, light);
         Geometry.triangle(consumer, last, p0, p1, bottom, dim, light);
      }

      pose.pop();
      VertexConsumer core = ctx.buffers().getBuffer(CloudVisualsRenderTypes.glowSprite(CosmeticTextures.dot()));
      Geometry.dotSprite(core, pose.peek(), new Vector3f(), rightOf(ctx), upOf(ctx), 0.14F, this.color(0.5F, time, 0.7F * fade * strength));
   }

   private void droplet(CosmeticContext ctx, MatrixStack pose, float time, int light, float fade) {
      VertexConsumer consumer = ctx.buffers().getBuffer(CloudVisualsRenderTypes.lit(CosmeticTextures.white()));
      Entry last = pose.peek();
      int rings = 10;
      int segments = 16;
      float breath = 1.0F + 0.04F * MathHelper.sin(time * 3.0F);

      for (int ring = 0; ring < rings; ring++) {
         float v0 = (float)ring / rings;
         float v1 = (float)(ring + 1) / rings;

         for (int s = 0; s < segments; s++) {
            float u0 = (float)s / segments * (float) (Math.PI * 2);
            float u1 = (float)(s + 1) / segments * (float) (Math.PI * 2);
            Vector3f p00 = dropletPoint(v0, u0, breath);
            Vector3f p01 = dropletPoint(v0, u1, breath);
            Vector3f p11 = dropletPoint(v1, u1, breath);
            Vector3f p10 = dropletPoint(v1, u0, breath);
            int top = this.solid(v0 * 0.5F, time, 0.85F * fade, 0.55F * (1.0F - v0));
            int bottomColor = this.solid(v1 * 0.5F, time, 0.85F * fade, 0.55F * (1.0F - v1));
            Geometry.quadUv(consumer, last, p00, p01, p11, p10, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, top, top, bottomColor, bottomColor, light);
         }
      }
   }

   private static Vector3f dropletPoint(float v, float u, float breath) {
      float angle = v * (float) Math.PI;
      float radius = MathHelper.sin(angle) * 0.12F * (0.55F + 0.45F * v) * breath;
      float y = MathHelper.cos(angle) * 0.17F + (1.0F - v) * 0.05F;
      return new Vector3f(MathHelper.cos(u) * radius, y, MathHelper.sin(u) * radius);
   }

   private void cube(CosmeticContext ctx, MatrixStack pose, float time, int light, float fade, float strength) {
      pose.push();
      pose.multiply(RotationAxis.POSITIVE_Y.rotation(time * 0.9F));
      pose.multiply(RotationAxis.POSITIVE_X.rotation(MathHelper.sin(time * 0.7F) * 0.5F));
      Entry last = pose.peek();
      VertexConsumer consumer = ctx.buffers().getBuffer(CloudVisualsRenderTypes.lit(CosmeticTextures.white()));
      float s = 0.11F;
      Vector3f[] v = new Vector3f[]{
         new Vector3f(-s, -s, -s),
         new Vector3f(s, -s, -s),
         new Vector3f(s, s, -s),
         new Vector3f(-s, s, -s),
         new Vector3f(-s, -s, s),
         new Vector3f(s, -s, s),
         new Vector3f(s, s, s),
         new Vector3f(-s, s, s)
      };
      int[][] faces = new int[][]{{0, 3, 2, 1}, {4, 5, 6, 7}, {0, 1, 5, 4}, {3, 7, 6, 2}, {0, 4, 7, 3}, {1, 2, 6, 5}};

      for (int i = 0; i < faces.length; i++) {
         int[] f = faces[i];
         Geometry.quad(consumer, last, v[f[0]], v[f[1]], v[f[2]], v[f[3]], this.solid(i / 6.0F, time, 0.9F * fade, 0.15F), light);
      }

      pose.pop();
      VertexConsumer edges = ctx.buffers().getBuffer(CloudVisualsRenderTypes.glowSprite(CosmeticTextures.dot()));
      Geometry.dotSprite(edges, pose.peek(), new Vector3f(), rightOf(ctx), upOf(ctx), 0.22F, this.color(0.3F, time, 0.35F * fade * strength));
   }

   private void star(CosmeticContext ctx, MatrixStack pose, float time, int light, float fade, float strength) {
      pose.push();
      pose.multiply(RotationAxis.POSITIVE_Y.rotation(time * 1.8F));
      Entry last = pose.peek();
      VertexConsumer consumer = ctx.buffers().getBuffer(CloudVisualsRenderTypes.lit(CosmeticTextures.white()));
      int points = 5;
      float outer = 0.2F;
      float inner = 0.085F;
      float depth = 0.05F;
      Vector3f front = new Vector3f(0.0F, 0.0F, depth);
      Vector3f back = new Vector3f(0.0F, 0.0F, -depth);

      for (int i = 0; i < points * 2; i++) {
         double a0 = (Math.PI / 2) + Math.PI * i / points;
         double a1 = (Math.PI / 2) + Math.PI * (i + 1) / points;
         float r0 = i % 2 == 0 ? outer : inner;
         float r1 = i % 2 == 0 ? inner : outer;
         Vector3f p0 = new Vector3f((float)Math.cos(a0) * r0, (float)Math.sin(a0) * r0, 0.0F);
         Vector3f p1 = new Vector3f((float)Math.cos(a1) * r1, (float)Math.sin(a1) * r1, 0.0F);
         int bright = this.solid(i / 10.0F, time, 0.95F * fade, i % 2 == 0 ? 0.3F : 0.1F);
         int dim = this.solid(i / 10.0F, time, 0.95F * fade, 0.0F);
         Geometry.triangle(consumer, last, p0, p1, front, bright, light);
         Geometry.triangle(consumer, last, p1, p0, back, dim, light);
      }

      pose.pop();
      VertexConsumer core = ctx.buffers().getBuffer(CloudVisualsRenderTypes.glowSprite(CosmeticTextures.dot()));
      Geometry.dotSprite(core, pose.peek(), new Vector3f(), rightOf(ctx), upOf(ctx), 0.2F, this.color(0.1F, time, 0.55F * fade * strength));
   }

   private static Vector3f rightOf(CosmeticContext ctx) {
      return ctx.right();
   }

   private static Vector3f upOf(CosmeticContext ctx) {
      return ctx.up();
   }

   public enum Pet {
      KING,
      CAT,
      DRAGON,
      GHOST,
      BEE,
      SLIME,
      FAIRY,
      WISP,
      CRYSTAL,
      DROPLET,
      CUBE,
      STAR;

      Pet() {
      }
   }

   public enum Side {
      LEFT,
      RIGHT;

      Side() {
      }
   }
}
