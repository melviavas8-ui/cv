package dev.cloudvisuals.module.impl.visual;

import dev.cloudvisuals.hud.TargetTracker;
import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.render.CloudVisualsRenderTypes;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.world.CosmeticContext;
import dev.cloudvisuals.render.world.CosmeticTextures;
import dev.cloudvisuals.render.world.Geometry;
import dev.cloudvisuals.setting.ChoiceSetting;
import dev.cloudvisuals.setting.NumberSetting;
import dev.cloudvisuals.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class TargetEspModule extends Module {
   private final ChoiceSetting<TargetEspModule.Style> style = this.add(new ChoiceSetting<>("style", TargetEspModule.Style.GHOSTS));
   private final NumberSetting keep = this.add(new NumberSetting("keep", 2500.0, 500.0, 8000.0, 100.0, "cloudvisuals.unit.ms"));
   private final NumberSetting amount = this.add(new NumberSetting("amount", 8.0, 3.0, 24.0, 1.0, ""));
   private final NumberSetting size = this.add(new NumberSetting("size", 100.0, 50.0, 200.0, 5.0, "cloudvisuals.unit.percent"));
   private final NumberSetting speed = this.add(new NumberSetting("speed", 100.0, 0.0, 300.0, 5.0, "cloudvisuals.unit.percent"));
   private final NumberSetting radius = this.add(new NumberSetting("radius", 1.0, 0.4, 2.5, 0.05, ""));
   private float visibility;
   private double effectPhase;
   private LivingEntity lastTarget;

   public TargetEspModule() {
      super("target_esp", Category.VISUALS, false);
   }

   @Override
   public int icon() {
      return 58181;
   }

   public void render(CosmeticContext ctx) {
      MinecraftClient mc = MinecraftClient.getInstance();
      LivingEntity target = TargetTracker.target(this.keep.intValue(), true);
      if (target != null) {
         this.lastTarget = target;
      }

      boolean valid = target != null && target != mc.player;
      float dt = ctx.delta();
      this.visibility = MathHelper.clamp(this.visibility + (valid ? dt * 6.0F : -dt * 6.0F), 0.0F, 1.0F);
      this.effectPhase = this.effectPhase + dt * this.speed.floatValue() / 100.0;
      LivingEntity renderEntity = valid ? target : this.lastTarget;
      if (renderEntity != null && !(this.visibility <= 0.01F)) {
         float partial = ctx.partialTick();
         Vec3d pos = new Vec3d(
            MathHelper.lerp(partial, renderEntity.lastX, renderEntity.getX()),
            MathHelper.lerp(partial, renderEntity.lastY, renderEntity.getY()),
            MathHelper.lerp(partial, renderEntity.lastZ, renderEntity.getZ())
         );
         Vec3d center = pos.add(0.0, renderEntity.getHeight() * 0.55, 0.0);
         MatrixStack pose = ctx.pose();
         Vector3f at = ctx.local(center.x, center.y, center.z);
         pose.push();
         pose.translate(at.x, at.y, at.z);
         float time = (float)this.effectPhase;
         float s = this.size.floatValue() / 100.0F;
         float r = (float)(renderEntity.getWidth() * 0.9 + 0.3);
         float h = renderEntity.getHeight();
         int base = ColorUtil.lerp(Theme.accent(), Theme.accent2(), 0.5F + 0.5F * (float)Math.sin(time * 1.4));
         switch ((TargetEspModule.Style)this.style.get()) {
            case GHOSTS:
               this.ghosts(ctx, pose, h, r, base, time, s);
               break;
            case SPIRAL:
               this.spiral(ctx, pose, h, r, base, time, s);
               break;
            case RING:
               this.ring(ctx, pose, h, r, base, time, s);
               break;
            case CUBES:
               this.cubes(ctx, pose, h, r, base, time, s);
               break;
            case COMBO:
               this.ghosts(ctx, pose, h, r, base, time, s);
               this.ring(ctx, pose, h, r, base, time * 0.7F, s);
         }

         pose.pop();
      } else {
         if (!valid) {
            this.lastTarget = null;
         }
      }
   }

   private void ghosts(CosmeticContext ctx, MatrixStack pose, float h, float r, int color, float time, float s) {
      VertexConsumer glow = ctx.buffers().getBuffer(CloudVisualsRenderTypes.glowSprite(CosmeticTextures.dot()));
      int n = this.amount.intValue();
      float radius = r * this.radius.floatValue() * (1.2F - 0.4F * this.visibility);

      for (int i = 0; i < n; i++) {
         double deg = 360.0 * i / n + time * 60.0;
         double th = Math.toRadians(deg);
         float oy = h * 0.55F + h * 0.35F * (float)Math.sin(Math.toRadians(deg * 0.5 + time * 12.0));
         Vector3f c = new Vector3f((float)(Math.sin(th) * radius), oy, (float)(Math.cos(th) * radius));
         float pulse = 0.5F + 0.5F * (float)Math.sin(Math.toRadians(deg * 3.0));
         Geometry.dotSprite(glow, pose.peek(), c, ctx.right(), ctx.up(), (0.45F + 0.18F * pulse) * s, fade(color, this.visibility * 0.1F));
         Geometry.dotSprite(glow, pose.peek(), c, ctx.right(), ctx.up(), (0.12F + 0.1F * pulse) * s, fade(color, this.visibility * (0.5F + 0.5F * pulse)));
      }
   }

   private void spiral(CosmeticContext ctx, MatrixStack pose, float h, float r, int color, float time, float s) {
      VertexConsumer glow = ctx.buffers().getBuffer(CloudVisualsRenderTypes.glowSprite(CosmeticTextures.dot()));
      int arms = 3;
      int trail = 8;
      float radius = r * this.radius.floatValue();
      float waveH = h * 0.22F;
      float cy = -h * 0.05F;

      for (int arm = 0; arm < arms; arm++) {
         double armOff = 360.0 * arm / arms;

         for (int sample = 0; sample <= trail; sample++) {
            float t = (float)sample / trail;
            double deg = (sample / 1.5 + time * 20.0) * 8.0 + armOff;
            double th = Math.toRadians(deg);
            double waveDeg = (time * 20.0 + sample * (arm + 1)) * 3.0;
            Vector3f c = new Vector3f((float)(Math.cos(th) * radius), cy + (float)Math.sin(Math.toRadians(waveDeg)) * waveH, (float)(Math.sin(th) * radius));
            Geometry.dotSprite(glow, pose.peek(), c, ctx.right(), ctx.up(), Math.max(0.1F, 0.16F * t) * s, fade(color, this.visibility * t));
         }
      }
   }

   private void ring(CosmeticContext ctx, MatrixStack pose, float h, float r, int color, float time, float s) {
      VertexConsumer glow = ctx.buffers().getBuffer(CloudVisualsRenderTypes.glowSprite(CosmeticTextures.dot()));
      float radius = r * this.radius.floatValue() * 1.1F * s;
      double e = time % 2.0;
      boolean descending = e > 1.0;
      double prog = descending ? e - 1.0 : 1.0 - e;
      prog = prog < 0.5 ? 2.0 * prog * prog : 1.0 - Math.pow(-2.0 * prog + 2.0, 2.0) * 0.5;
      float ringY = (float)(h * prog) - h * 0.5F;
      float trail = (float)(h / 1.2 * (prog > 0.5 ? 1.0 - prog : prog) * (descending ? -1.0 : 1.0));
      pose.push();
      pose.multiply(new Quaternionf().rotationX(0.09424778F));

      for (int seg = 0; seg <= 64; seg++) {
         double th = (Math.PI * 2) * seg / 64.0;
         float x = (float)Math.cos(th) * radius;
         float z = (float)Math.sin(th) * radius;
         Geometry.dotSprite(glow, pose.peek(), new Vector3f(x, ringY, z), ctx.right(), ctx.up(), 0.07F * s, fade(color, this.visibility * 0.85F));
         Geometry.dotSprite(glow, pose.peek(), new Vector3f(x, ringY + trail, z), ctx.right(), ctx.up(), 0.05F * s, fade(color, this.visibility * 0.02F));
      }

      pose.pop();
   }

   private void cubes(CosmeticContext ctx, MatrixStack pose, float h, float r, int color, float time, float s) {
      VertexConsumer glow = ctx.buffers().getBuffer(CloudVisualsRenderTypes.glowSprite(CosmeticTextures.dot()));
      int n = this.amount.intValue();
      float radius = r * this.radius.floatValue() * 1.2F;

      for (int i = 0; i < n; i++) {
         double deg = 360.0 * i / n + time * 40.0;
         double th = Math.toRadians(deg);
         float oy = (float)(Math.abs(Math.sin(th * 2.0 + time)) * h * 0.8F);
         Vector3f c = new Vector3f((float)(Math.cos(th) * radius), oy - h * 0.2F, (float)(Math.sin(th) * radius));
         float flick = 0.5F + 0.5F * (float)Math.sin(time * 5.0 + i * 2.0);
         Geometry.dotSprite(glow, pose.peek(), c, ctx.right(), ctx.up(), 0.09F * s * (0.6F + 0.6F * flick), fade(color, this.visibility * flick));
      }
   }

   private static int fade(int color, float amount) {
      return ColorUtil.withAlpha(color, Math.round(ColorUtil.alpha(color) * MathHelper.clamp(amount, 0.0F, 1.0F)));
   }

   public enum Style {
      GHOSTS,
      SPIRAL,
      RING,
      CUBES,
      COMBO;

      Style() {
      }
   }
}
