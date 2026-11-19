package dev.cloudvisuals.module.impl.cosmetic;

import dev.cloudvisuals.render.CloudVisualsRenderTypes;
import dev.cloudvisuals.render.world.CosmeticContext;
import dev.cloudvisuals.render.world.CosmeticTextures;
import dev.cloudvisuals.render.world.Geometry;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.ChoiceSetting;
import dev.cloudvisuals.setting.NumberSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

public final class JumpCirclesModule extends CosmeticModule {
   private static final int MAX = 16;
   private final ChoiceSetting<JumpCirclesModule.Style> style = this.add(new ChoiceSetting<>("style", JumpCirclesModule.Style.RING));
   private final ChoiceSetting<JumpCirclesModule.Trigger> trigger = this.add(new ChoiceSetting<>("trigger", JumpCirclesModule.Trigger.JUMP));
   private final NumberSetting radius;
   private final NumberSetting duration;
   private final NumberSetting thickness;
   private final NumberSetting spin;
   private final BoolSetting fill;
   private final NumberSetting glow;
   private final List<JumpCirclesModule.Circle> circles;
   private boolean wasOnGround;
   private double falling;
   private int counter;

   public JumpCirclesModule() {
      super("jump_circles");
      this.section("shape", 58660);
      this.radius = this.add(new NumberSetting("radius", 100.0, 30.0, 300.0, 5.0, "cloudvisuals.unit.percent"));
      this.duration = this.add(new NumberSetting("duration", 900.0, 200.0, 3000.0, 50.0, "cloudvisuals.unit.ms"));
      this.thickness = this.add(new NumberSetting("thickness", 100.0, 30.0, 300.0, 5.0, "cloudvisuals.unit.percent"));
      this.spin = this.add(new NumberSetting("spin", 90.0, -360.0, 360.0, 10.0, "cloudvisuals.unit.degrees_per_second"));
      this.fill = this.add(new BoolSetting("fill", true));
      this.glow = this.add(new NumberSetting("glow", 80.0, 0.0, 100.0, 5.0, "cloudvisuals.unit.percent"));
      this.circles = new ArrayList<>();
      this.wasOnGround = true;
   }

   @Override
   public int icon() {
      return 58181;
   }

   @Override
   protected boolean alive() {
      return true;
   }

   @Override
   protected void onDisable() {
      this.circles.clear();
   }

   @Override
   public void onTick() {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null) {
         boolean onGround = player.isOnGround();
         boolean jumped = this.wasOnGround && !onGround && player.getVelocity().y > 0.2;
         boolean landed = !this.wasOnGround && onGround;
         JumpCirclesModule.Trigger mode = this.trigger.get();
         if (jumped && mode != JumpCirclesModule.Trigger.LAND || landed && mode != JumpCirclesModule.Trigger.JUMP) {
            float power = landed ? (float)Math.clamp(-this.falling / 0.9, 0.7, 1.7) : 1.0F;
            this.circles.add(new JumpCirclesModule.Circle(player.getX(), player.getY(), player.getZ(), System.currentTimeMillis(), this.counter++, power));
            if (this.circles.size() > 16) {
               this.circles.removeFirst();
            }
         }

         this.wasOnGround = onGround;
         this.falling = player.getVelocity().y;
         long now = System.currentTimeMillis();
         this.circles.removeIf(circle -> now - circle.start() > this.duration.get() + 200.0);
      }
   }

   public void render(CosmeticContext ctx) {
      if (!this.circles.isEmpty()) {
         long now = System.currentTimeMillis();
         float time = ctx.time();
         float scale = this.radius.floatValue() / 100.0F;
         float strength = 0.35F + 0.65F * this.glow.floatValue() / 100.0F;
         float width = this.thickness.floatValue() / 100.0F;
         MatrixStack pose = ctx.pose();
         JumpCirclesModule.Style current = this.style.get();
         VertexConsumer rings = ctx.buffers().getBuffer(CloudVisualsRenderTypes.glowSprite(CosmeticTextures.ring()));

         for (JumpCirclesModule.Circle circle : this.circles) {
            float t = Math.clamp((float)(now - circle.start()) / this.duration.floatValue(), 0.0F, 1.0F);
            if (!(t >= 1.0F)) {
               pose.push();
               translate(ctx, pose, circle);
               Entry last = pose.peek();
               float angle = time * this.spin.floatValue() * (float) (Math.PI / 180.0);
               float s = scale * this.vary(circle);
               switch (current) {
                  case WAVE:
                     for (int k = 0; k < 5; k++) {
                        float delay = k * 0.09F;
                        if (t > delay) {
                           float wt = Math.clamp((t - delay) / (1.0F - delay), 0.0F, 1.0F);
                           this.ringSprite(
                              rings,
                              last,
                              wt,
                              k * 0.05F,
                              s * (0.5F + 0.38F * k / 4.0F + wt * 0.55F),
                              k % 2 == 0 ? angle : -angle,
                              time,
                              strength * (1.0F - wt) * (1.0F - k * 0.12F)
                           );
                        }
                     }
                     break;
                  case RING:
                     this.ringSprite(rings, last, t, 0.0F, s, angle, time, strength);
                     break;
                  case DOUBLE:
                     this.ringSprite(rings, last, t, 0.0F, s, angle, time, strength);
                     this.ringSprite(rings, last, t, 0.14F, s * 0.62F, -angle, time, strength);
                     break;
                  case RIPPLES:
                     for (int k = 0; k < 3; k++) {
                        this.ringSprite(rings, last, t, k * 0.16F, s * (1.0F - k * 0.12F), angle, time, strength * (1.0F - k * 0.2F));
                     }
               }

               pose.pop();
            }
         }

         if (this.fill.isOn()) {
            VertexConsumer dots = ctx.buffers().getBuffer(CloudVisualsRenderTypes.glowSprite(CosmeticTextures.dot()));

            for (JumpCirclesModule.Circle circle : this.circles) {
               float t = Math.clamp((float)(now - circle.start()) / this.duration.floatValue(), 0.0F, 1.0F);
               if (!(t >= 1.0F)) {
                  pose.push();
                  translate(ctx, pose, circle);
                  float eased = 1.0F - (float)Math.pow(1.0F - t, 3.0);
                  Geometry.flatSprite(
                     dots,
                     pose.peek(),
                     0.0F,
                     -0.002F,
                     0.0F,
                     (0.3F + 1.0F * eased) * scale * this.vary(circle),
                     0.0F,
                     this.color(0.5F, time, 0.28F * (1.0F - t) * strength)
                  );
                  pose.pop();
               }
            }
         }

         if (current == JumpCirclesModule.Style.DASHED || current == JumpCirclesModule.Style.HEXAGON || current == JumpCirclesModule.Style.STAR) {
            VertexConsumer lines = ctx.buffers().getBuffer(CloudVisualsRenderTypes.GLOW);

            for (JumpCirclesModule.Circle circle : this.circles) {
               float t = Math.clamp((float)(now - circle.start()) / this.duration.floatValue(), 0.0F, 1.0F);
               if (!(t >= 1.0F)) {
                  pose.push();
                  translate(ctx, pose, circle);
                  Entry last = pose.peek();
                  float eased = 1.0F - (float)Math.pow(1.0F - t, 3.0);
                  float r = (0.28F + 1.02F * eased) * scale * this.vary(circle);
                  float alpha = (float)Math.pow(1.0F - t, 1.6) * strength;
                  float angle = time * this.spin.floatValue() * (float) (Math.PI / 180.0);
                  float line = 0.045F * width;
                  switch (current) {
                     case DASHED:
                        this.dashes(lines, last, r, line, angle, alpha, time);
                        break;
                     case HEXAGON:
                        this.polygon(lines, last, r, line, angle, 6, 1, alpha, time);
                        break;
                     case STAR:
                        this.polygon(lines, last, r, line, angle, 3, 1, alpha, time);
                        this.polygon(lines, last, r, line, angle + (float) Math.PI, 3, 1, alpha, time);
                  }

                  pose.pop();
               }
            }
         }

         ctx.buffers().draw();
      }
   }

   private float vary(JumpCirclesModule.Circle circle) {
      float strength = this.life();
      if (strength <= 0.0F) {
         return 1.0F;
      }

      float spread = circle.seed() * 0.6180339F % 1.0F;
      return MathHelper.lerp(strength, 1.0F, (1.0F + (spread - 0.5F) * 0.24F) * circle.power());
   }

   private static void translate(CosmeticContext ctx, MatrixStack pose, JumpCirclesModule.Circle circle) {
      Vector3f at = ctx.local(circle.x(), circle.y() + 0.02, circle.z());
      pose.translate(at.x, at.y, at.z);
   }

   private void ringSprite(VertexConsumer consumer, Entry pose, float t, float delay, float scale, float angle, float time, float strength) {
      float local = Math.clamp((t - delay) / (1.0F - delay), 0.0F, 1.0F);
      if (!(local <= 0.0F)) {
         float eased = 1.0F - (float)Math.pow(1.0F - local, 3.0);
         float r = (0.25F + 1.05F * eased) * scale;
         float alpha = (float)Math.pow(1.0F - local, 1.6);
         float half = r / (0.8F - 0.06F * (this.thickness.floatValue() / 100.0F - 1.0F));
         Geometry.flatSprite(consumer, pose, 0.0F, 0.0F, 0.0F, half, angle, this.color(local * 0.5F, time, alpha * strength));
      }
   }

   private void dashes(VertexConsumer consumer, Entry pose, float r, float line, float angle, float alpha, float time) {
      int count = 20;

      for (int i = 0; i < count; i++) {
         float t = (float)i / count;
         double a0 = t * Math.PI * 2.0 + angle;
         double a1 = a0 + (Math.PI * 2) / count * 0.55;
         float inner = r - line;
         float outer = r + line;
         float c0 = (float)Math.cos(a0);
         float s0 = (float)Math.sin(a0);
         float c1 = (float)Math.cos(a1);
         float s1 = (float)Math.sin(a1);
         int c = this.color(t, time, alpha);
         Geometry.glowQuad(
            consumer,
            pose,
            new Vector3f(c0 * inner, 0.0F, s0 * inner),
            new Vector3f(c0 * r, 0.0F, s0 * r),
            new Vector3f(c1 * r, 0.0F, s1 * r),
            new Vector3f(c1 * inner, 0.0F, s1 * inner),
            c & 16777215,
            c,
            c,
            c & 16777215
         );
         Geometry.glowQuad(
            consumer,
            pose,
            new Vector3f(c0 * r, 0.0F, s0 * r),
            new Vector3f(c0 * outer, 0.0F, s0 * outer),
            new Vector3f(c1 * outer, 0.0F, s1 * outer),
            new Vector3f(c1 * r, 0.0F, s1 * r),
            c,
            c & 16777215,
            c & 16777215,
            c
         );
      }
   }

   private void polygon(VertexConsumer consumer, Entry pose, float r, float line, float angle, int sides, int step, float alpha, float time) {
      for (int i = 0; i < sides; i++) {
         float t0 = (float)i / sides;
         float t1 = (float)(i + step) / sides;
         double a0 = t0 * Math.PI * 2.0 + angle;
         double a1 = t1 * Math.PI * 2.0 + angle;
         Vector3f p0 = new Vector3f((float)Math.cos(a0) * r, 0.0F, (float)Math.sin(a0) * r);
         Vector3f p1 = new Vector3f((float)Math.cos(a1) * r, 0.0F, (float)Math.sin(a1) * r);
         Vector3f direction = new Vector3f(p1).sub(p0).normalize();
         Vector3f across = new Vector3f(-direction.z, 0.0F, direction.x).mul(line);
         int c0 = this.color(t0, time, alpha);
         int c1 = this.color(t1, time, alpha);
         Geometry.glowQuad(
            consumer,
            pose,
            new Vector3f(p0).sub(across),
            new Vector3f(p0),
            new Vector3f(p1),
            new Vector3f(p1).sub(across),
            c0 & 16777215,
            c0,
            c1,
            c1 & 16777215
         );
         Geometry.glowQuad(
            consumer,
            pose,
            new Vector3f(p0),
            new Vector3f(p0).add(across),
            new Vector3f(p1).add(across),
            new Vector3f(p1),
            c0,
            c0 & 16777215,
            c1 & 16777215,
            c1
         );
      }
   }

   private record Circle(double x, double y, double z, long start, float seed, float power) {
      private Circle {
      }
   }

   public enum Style {
      WAVE,
      RING,
      DOUBLE,
      RIPPLES,
      DASHED,
      HEXAGON,
      STAR;

      Style() {
      }
   }

   public enum Trigger {
      JUMP,
      LAND,
      BOTH;

      Trigger() {
      }
   }
}
