package dev.cloudvisuals.module.impl.cosmetic;

import dev.cloudvisuals.render.CloudVisualsRenderTypes;
import dev.cloudvisuals.render.world.CosmeticContext;
import dev.cloudvisuals.render.world.CosmeticTextures;
import dev.cloudvisuals.render.world.Geometry;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.ChoiceSetting;
import dev.cloudvisuals.setting.NumberSetting;
import dev.cloudvisuals.util.ColorUtil;
import dev.cloudvisuals.util.EntityLayers;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

public final class AfterimageModule extends CosmeticModule {
   private final ChoiceSetting<AfterimageModule.Style> style = this.add(new ChoiceSetting<>("style", AfterimageModule.Style.GHOST));
   private final NumberSetting lifetime = this.add(new NumberSetting("lifetime", 1.0, 0.3, 3.0, 0.1, "cloudvisuals.unit.seconds"));
   private final NumberSetting limit = this.add(new NumberSetting("limit", 8.0, 1.0, 20.0, 1.0, ""));
   private final NumberSetting rise = this.add(new NumberSetting("rise", 100.0, 0.0, 300.0, 10.0, "cloudvisuals.unit.percent"));
   private final BoolSetting dust = this.add(new BoolSetting("dust", true));
   private final BoolSetting onJump;
   private final BoolSetting onLand;
   private final BoolSetting onSprint;
   private final NumberSetting interval;
   private final BoolSetting onHit;
   private final List<AfterimageModule.Echo> echoes;
   private static final double MOTE_WIND = 0.55;
   private final List<double[]> motes;
   private final Vector3f wind;
   private final Random random;
   private boolean wasOnGround;
   private int airTicks;
   private int sprintTicks;
   private float moteBudget;
   private PlayerEntityModel wide;
   private PlayerEntityModel slim;

   public AfterimageModule() {
      super("afterimage");
      this.section("rules", 57688);
      this.onJump = this.add(new BoolSetting("on_jump", true));
      this.onLand = this.add(new BoolSetting("on_land", true));
      this.onSprint = this.add(new BoolSetting("on_sprint", false));
      this.interval = this.add(new NumberSetting("interval", 0.25, 0.1, 1.0, 0.05, "cloudvisuals.unit.seconds"));
      this.onHit = this.add(new BoolSetting("on_hit", false));
      this.echoes = new ArrayList<>();
      this.motes = new ArrayList<>();
      this.wind = new Vector3f();
      this.random = new Random();
      this.wasOnGround = true;
      this.interval.visibleWhen(this.onSprint::isOn);
   }

   @Override
   public int icon() {
      return 58297;
   }

   @Override
   protected boolean alive() {
      return true;
   }

   @Override
   protected void onDisable() {
      this.echoes.clear();
      this.motes.clear();
   }

   @Override
   public void onTick() {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null) {
         boolean ground = player.isOnGround();
         if (!ground) {
            this.airTicks++;
         }

         if (this.wasOnGround && !ground && player.getVelocity().y > 0.2 && this.onJump.isOn()) {
            this.capture(player);
         }

         if (!this.wasOnGround && ground && this.airTicks > 5 && this.onLand.isOn()) {
            this.capture(player);
         }

         if (ground) {
            this.airTicks = 0;
         }

         this.wasOnGround = ground;
         if (this.onSprint.isOn() && player.isSprinting()) {
            if (++this.sprintTicks >= Math.max(2, Math.round(this.interval.floatValue() * 20.0F))) {
               this.sprintTicks = 0;
               this.capture(player);
            }
         } else {
            this.sprintTicks = 0;
         }
      }
   }

   public void onConfirmedHit() {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (this.isEnabled() && this.onHit.isOn() && player != null) {
         this.capture(player);
      }
   }

   public void captureNow() {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null) {
         this.capture(player);
      }
   }

   private PlayerEntityModel model(AbstractClientPlayerEntity player) {
      boolean thin = player.getSkin().model() == PlayerSkinType.SLIM;
      if (thin) {
         if (this.slim == null) {
            this.slim = new PlayerEntityModel(MinecraftClient.getInstance().getLoadedEntityModels().getModelPart(EntityLayers.playerSlim()), true);
         }

         return this.slim;
      } else {
         if (this.wide == null) {
            this.wide = new PlayerEntityModel(MinecraftClient.getInstance().getLoadedEntityModels().getModelPart(EntityModelLayers.PLAYER), false);
         }

         return this.wide;
      }
   }

   private void capture(ClientPlayerEntity player) {
      if (!player.isSpectator() && !player.isInvisible() && !player.isGliding() && !player.isInSwimmingPose() && !player.isSleeping() && !player.hasVehicle()) {
         PlayerEntityModel model = this.model(player);
         float partial = 1.0F;
         float bodyYaw = MathHelper.lerpAngleDegrees(partial, player.lastBodyYaw, player.bodyYaw);
         if (MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof PlayerEntityRenderer renderer) {
            PlayerEntityRenderState var8 = (PlayerEntityRenderState)renderer.getAndUpdateRenderState(player, partial);
            model.setAngles(var8);
            AfterimageModule.Echo echo = new AfterimageModule.Echo();
            echo.x = player.getX();
            echo.y = player.getY();
            echo.z = player.getZ();
            echo.yaw = bodyYaw;
            echo.crouching = var8.isInSneakingPose;
            echo.light = MinecraftClient.getInstance().getEntityRenderDispatcher().getLight(player, partial);
            echo.head = model.head.getTransform();
            echo.body = model.body.getTransform();
            echo.rightArm = model.rightArm.getTransform();
            echo.leftArm = model.leftArm.getTransform();
            echo.rightLeg = model.rightLeg.getTransform();
            echo.leftLeg = model.leftLeg.getTransform();
            echo.hat = player.isModelPartVisible(PlayerModelPart.HAT);
            echo.jacket = player.isModelPartVisible(PlayerModelPart.JACKET);
            echo.leftSleeve = player.isModelPartVisible(PlayerModelPart.LEFT_SLEEVE);
            echo.rightSleeve = player.isModelPartVisible(PlayerModelPart.RIGHT_SLEEVE);
            echo.leftPants = player.isModelPartVisible(PlayerModelPart.LEFT_PANTS_LEG);
            echo.rightPants = player.isModelPartVisible(PlayerModelPart.RIGHT_PANTS_LEG);
            this.echoes.add(echo);

            while (this.echoes.size() > this.limit.intValue()) {
               this.echoes.removeFirst();
            }
         }
      }
   }

   public boolean active() {
      return !this.echoes.isEmpty() || !this.motes.isEmpty();
   }

   public void render(CosmeticContext ctx) {
      float dt = ctx.delta();
      float life = this.lifetime.floatValue();
      float time = ctx.time();

      for (int i = this.echoes.size() - 1; i >= 0; i--) {
         AfterimageModule.Echo echo = this.echoes.get(i);
         echo.age += dt;
         if (echo.age >= life) {
            this.echoes.remove(i);
         }
      }

      this.advanceMotes(dt, life, time);
      PlayerEntityModel model = this.model(ctx.player());
      if (!this.echoes.isEmpty()) {
         Identifier skin = ctx.player().getSkin().body().texturePath();

         RenderLayer type = switch ((AfterimageModule.Style)this.style.get()) {
            case GHOST -> RenderLayers.entityTranslucent(skin);
            case HOLOGRAM -> CloudVisualsRenderTypes.additiveEntity(CosmeticTextures.white());
            case STATUE, SHADOW -> RenderLayers.entityTranslucent(CosmeticTextures.white());
         };
         VertexConsumer consumer = ctx.buffers().getBuffer(type);
         float lift = this.rise.floatValue() / 100.0F;

         for (AfterimageModule.Echo echo : this.echoes) {
            this.draw(ctx, model, consumer, echo, echo.age / life, lift, time);
         }

         ctx.buffers().draw(type);
      }

      if (!this.motes.isEmpty()) {
         VertexConsumer dots = ctx.buffers().getBuffer(CloudVisualsRenderTypes.glowSprite(CosmeticTextures.dot()));

         for (double[] mote : this.motes) {
            Vector3f at = ctx.local(mote[0], mote[1], mote[2]);
            float t = (float)(mote[3] / mote[4]);
            float visible = (float)Math.min(1.0, mote[3] / 0.1) * (1.0F - t) * CosmeticContext.nearFade(at, 0.4F, 0.9F);
            Geometry.dotSprite(
               dots, ctx.pose().peek(), at, ctx.right(), ctx.up(), (float)mote[5] * (1.0F - 0.5F * t), this.color((float)mote[6], time, visible * 0.9F)
            );
         }

         ctx.buffers().draw();
      }
   }

   private void draw(CosmeticContext ctx, PlayerEntityModel model, VertexConsumer consumer, AfterimageModule.Echo echo, float t, float lift, float time) {
      float fade = this.style.get() == AfterimageModule.Style.STATUE ? 1.0F - Math.clamp((t - 0.45F) / 0.55F, 0.0F, 1.0F) : (float)Math.pow(1.0F - t, 1.5);
      Vector3f chest = ctx.local(echo.x, echo.y + 1.0, echo.z);
      float visible = fade * CosmeticContext.nearFade(chest, 0.7F, 1.5F) * this.opacity();
      if (!(visible <= 0.01F)) {
         int tint = this.rgb(t * 0.5F, time);
         int light = echo.light;

         int color = switch ((AfterimageModule.Style)this.style.get()) {
            case GHOST -> ColorUtil.withAlpha(ColorUtil.lerp(-1, tint, 0.6F), Math.round(255.0F * visible * 0.8F));
            case HOLOGRAM -> ColorUtil.withAlpha(ColorUtil.lerp(-16777216, tint, visible * 0.3F), 255);
            case STATUE -> ColorUtil.withAlpha(ColorUtil.lerp(-7565418, tint, 0.18F), Math.round(255.0F * visible));
            case SHADOW -> ColorUtil.withAlpha(-16448248, Math.round(255.0F * visible * 0.7F));
         };
         if (this.style.get() != AfterimageModule.Style.STATUE) {
            light = 15728880;
         }

         Vector3f at = ctx.local(echo.x, echo.y + t * t * 0.45 * lift, echo.z);
         float grow = 0.9375F * (1.0F + 0.05F * t * (this.style.get() == AfterimageModule.Style.STATUE ? 0 : 1));
         MatrixStack pose = ctx.pose();
         pose.push();
         pose.translate(at.x, at.y + (echo.crouching ? -0.125F : 0.0F), at.z);
         pose.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - echo.yaw));
         pose.scale(-1.0F, -1.0F, 1.0F);
         pose.scale(grow, grow, grow);
         pose.translate(0.0F, -1.501F, 0.0F);
         model.getRootPart().traverse().forEach(part -> part.visible = true);
         model.head.setTransform(echo.head);
         model.body.setTransform(echo.body);
         model.rightArm.setTransform(echo.rightArm);
         model.leftArm.setTransform(echo.leftArm);
         model.rightLeg.setTransform(echo.rightLeg);
         model.leftLeg.setTransform(echo.leftLeg);
         model.hat.visible = echo.hat;
         model.jacket.visible = echo.jacket;
         model.leftSleeve.visible = echo.leftSleeve;
         model.rightSleeve.visible = echo.rightSleeve;
         model.leftPants.visible = echo.leftPants;
         model.rightPants.visible = echo.rightPants;
         model.render(pose, consumer, light, OverlayTexture.DEFAULT_UV, color);
         pose.pop();
      }
   }

   private void advanceMotes(float dt, float life, float time) {
      for (int i = this.motes.size() - 1; i >= 0; i--) {
         double[] mote = this.motes.get(i);
         mote[3] += dt;
         if (mote[3] >= mote[4]) {
            this.motes.remove(i);
         } else {
            mote[1] += mote[7] * dt;
            this.flow(mote[0], mote[1], mote[2], time, this.wind);
            mote[0] += (Math.sin(mote[3] * 3.0 + mote[6] * 20.0) * 0.12 + this.wind.x * 0.55) * dt;
            mote[1] += this.wind.y * 0.55 * 0.5 * dt;
            mote[2] += this.wind.z * 0.55 * dt;
         }
      }

      if (this.dust.isOn() && !this.echoes.isEmpty() && !(dt <= 0.0F)) {
         this.moteBudget = this.moteBudget + dt * 16.0F * this.echoes.size();

         while (this.moteBudget >= 1.0F && this.motes.size() < 260) {
            this.moteBudget--;
            AfterimageModule.Echo echo = this.echoes.get(this.random.nextInt(this.echoes.size()));
            float t = echo.age / life;
            if (!(t < 0.15F)) {
               double angle = this.random.nextDouble() * Math.PI * 2.0;
               double radius = this.random.nextDouble() * 0.28;
               this.motes
                  .add(
                     new double[]{
                        echo.x + Math.cos(angle) * radius,
                        echo.y + this.random.nextDouble() * 1.8,
                        echo.z + Math.sin(angle) * radius,
                        0.0,
                        0.6 + this.random.nextDouble() * 0.7,
                        0.022 + this.random.nextDouble() * 0.03,
                        this.random.nextDouble(),
                        0.35 + this.random.nextDouble() * 0.6
                     }
                  );
            }
         }

         if (this.moteBudget > 4.0F) {
            this.moteBudget = 4.0F;
         }
      }
   }

   private static final class Echo {
      double x;
      double y;
      double z;
      float yaw;
      boolean crouching;
      int light;
      float age;
      ModelTransform head;
      ModelTransform body;
      ModelTransform rightArm;
      ModelTransform leftArm;
      ModelTransform rightLeg;
      ModelTransform leftLeg;
      boolean hat;
      boolean jacket;
      boolean leftSleeve;
      boolean rightSleeve;
      boolean leftPants;
      boolean rightPants;

      private Echo() {
      }
   }

   public enum Style {
      GHOST,
      HOLOGRAM,
      STATUE,
      SHADOW;

      Style() {
      }
   }
}
