package dev.cloudvisuals.gui.screen;

import dev.cloudvisuals.util.ColorUtil;
import dev.cloudvisuals.util.EntityLayers;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.DiffuseLighting.Type;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

public final class PlayerPreview {
   private static final float FOOT_PAD = 2.0F;
   private PlayerEntityModel wide;
   private PlayerEntityModel slim;

   public PlayerPreview() {
   }

   public static void register() {
      SpecialGuiElementRegistry.register(context -> new PlayerPreview.Renderer(context.vertexConsumers()));
   }

   public void render(DrawContext g, float centerX, float feetY, float height, float yaw, float headYaw, float headPitch, float time, float alpha) {
      if (!(alpha <= 0.01F) && !(height <= 1.0F)) {
         MinecraftClient mc = MinecraftClient.getInstance();
         SkinTextures skin = (SkinTextures)mc.getSkinProvider().supplySkinTextures(mc.getGameProfile(), false).get();
         PlayerEntityModel model = skin.model() == PlayerSkinType.SLIM ? this.slim() : this.wide();
         pose(model, headYaw, headPitch, time * 20.0F);
         float halfWidth = height * 0.5F;
         int x0 = MathHelper.floor(centerX - halfWidth);
         int x1 = MathHelper.ceil(centerX + halfWidth);
         int y0 = MathHelper.floor(feetY - height * 1.1F);
         int y1 = MathHelper.ceil(feetY + 2.0F);
         float scale = height / 2.0F;
         ScreenRect scissor = g.scissorStack.peekLast();
         ScreenRect rect = new ScreenRect(x0, y0, x1 - x0, y1 - y0);
         ScreenRect bounds = scissor != null ? scissor.intersection(rect) : rect;
         if (bounds != null) {
            float footLift = (y1 - feetY) / scale;
            float centerShift = (centerX - (x0 + x1) / 2.0F) / scale;
            g.state
               .addSpecialElement(
                  new PlayerPreview.State(model, skin.body().texturePath(), yaw, alpha, footLift, centerShift, x0, y0, x1, y1, scale, scissor, bounds)
               );
         }
      }
   }

   private static void pose(PlayerEntityModel model, float headYaw, float headPitch, float ticks) {
      MinecraftClient mc = MinecraftClient.getInstance();
      model.getRootPart().traverse().forEach(part -> part.visible = true);
      model.hat.visible = mc.options.isPlayerModelPartEnabled(PlayerModelPart.HAT);
      model.jacket.visible = mc.options.isPlayerModelPartEnabled(PlayerModelPart.JACKET);
      model.leftSleeve.visible = mc.options.isPlayerModelPartEnabled(PlayerModelPart.LEFT_SLEEVE);
      model.rightSleeve.visible = mc.options.isPlayerModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE);
      model.leftPants.visible = mc.options.isPlayerModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG);
      model.rightPants.visible = mc.options.isPlayerModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG);
      model.head.pitch = headPitch * (float) (Math.PI / 180.0);
      model.head.yaw = headYaw * (float) (Math.PI / 180.0);
      model.head.roll = 0.0F;
      model.body.pitch = model.body.yaw = model.body.roll = 0.0F;
      model.rightLeg.pitch = model.rightLeg.yaw = model.rightLeg.roll = 0.0F;
      model.leftLeg.pitch = model.leftLeg.yaw = model.leftLeg.roll = 0.0F;
      float swing = MathHelper.cos(ticks * 0.09F) * 0.05F + 0.05F;
      float lift = MathHelper.sin(ticks * 0.067F) * 0.05F;
      model.rightArm.pitch = lift;
      model.rightArm.yaw = 0.0F;
      model.rightArm.roll = swing;
      model.leftArm.pitch = -lift;
      model.leftArm.yaw = 0.0F;
      model.leftArm.roll = -swing;
   }

   private PlayerEntityModel wide() {
      if (this.wide == null) {
         this.wide = new PlayerEntityModel(MinecraftClient.getInstance().getLoadedEntityModels().getModelPart(EntityModelLayers.PLAYER), false);
      }

      return this.wide;
   }

   private PlayerEntityModel slim() {
      if (this.slim == null) {
         this.slim = new PlayerEntityModel(MinecraftClient.getInstance().getLoadedEntityModels().getModelPart(EntityLayers.playerSlim()), true);
      }

      return this.slim;
   }

   static final class Renderer extends SpecialGuiElementRenderer<PlayerPreview.State> {
      Renderer(Immediate buffers) {
         super(buffers);
      }

      public Class<PlayerPreview.State> getElementClass() {
         return PlayerPreview.State.class;
      }

      protected void render(PlayerPreview.State state, MatrixStack pose) {
         MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(Type.ENTITY_IN_UI);
         pose.translate(state.centerShift(), -state.footLift(), 0.0F);
         pose.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.yaw()));
         pose.translate(0.0F, -1.5F, 0.0F);
         state.model()
            .render(
               pose,
               this.vertexConsumers.getBuffer(RenderLayers.entityTranslucent(state.texture())),
               15728880,
               OverlayTexture.DEFAULT_UV,
               ColorUtil.withAlpha(-1, Math.round(state.alpha() * 255.0F))
            );
      }

      protected String getName() {
         return "Cloud Visuals player preview";
      }
   }

   record State(
      PlayerEntityModel model,
      Identifier texture,
      float yaw,
      float alpha,
      float footLift,
      float centerShift,
      int x0,
      int y0,
      int x1,
      int y1,
      float scale,
      @Nullable ScreenRect scissorArea,
      @Nullable ScreenRect bounds
   ) implements SpecialGuiElementRenderState {
      State {
      }

      public int x1() {
         return this.x0;
      }

      public int y1() {
         return this.y0;
      }

      public int x2() {
         return this.x1;
      }

      public int y2() {
         return this.y1;
      }
   }
}
