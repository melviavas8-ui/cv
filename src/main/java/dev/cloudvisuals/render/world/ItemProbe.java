package dev.cloudvisuals.render.world;

import dev.cloudvisuals.render.CloudVisualsRenderTypes;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.command.OrderedRenderCommandQueue.Custom;
import net.minecraft.client.render.command.OrderedRenderCommandQueue.LayeredCustom;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState.LeashData;
import net.minecraft.client.render.entity.state.EntityRenderState.ShadowPiece;
import net.minecraft.client.render.item.ItemRenderState.Glint;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class ItemProbe implements OrderedRenderCommandQueue {
   @Nullable
   private OrderedRenderCommandQueue target;
   @Nullable
   private RenderLayer silhouetteType;
   private int color;
   private final Matrix4f pose = new Matrix4f();
   private boolean found;

   public ItemProbe() {
   }

   public ItemProbe capture() {
      this.target = null;
      this.silhouetteType = null;
      this.found = false;
      return this;
   }

   public ItemProbe silhouette(OrderedRenderCommandQueue target, RenderLayer type, int color) {
      this.target = target;
      this.silhouetteType = type;
      this.color = color;
      this.found = false;
      return this;
   }

   public boolean found() {
      return this.found;
   }

   public Matrix4f pose() {
      return this.pose;
   }

   public void submitItem(
      MatrixStack matrices,
      ItemDisplayContext displayContext,
      int light,
      int overlay,
      int outlineColors,
      int[] tintLayers,
      List<BakedQuad> quads,
      RenderLayer renderLayer,
      Glint glintType
   ) {
      if (!this.found) {
         this.pose.set(matrices.peek().getPositionMatrix());
         this.found = true;
      }

      if (this.target != null && this.silhouetteType != null && atlas(renderLayer)) {
         int argb = this.color;
         List<BakedQuad> copy = List.copyOf(quads);
         RenderLayer type = renderLayer == TexturedRenderLayers.getItemTranslucentCull() ? CloudVisualsRenderTypes.ITEM_SILHOUETTE_ITEMS : this.silhouetteType;
         this.target.submitCustom(matrices, type, (last, consumer) -> {
            ItemProbe.Flat flat = new ItemProbe.Flat(consumer, argb);

            for (BakedQuad quad : copy) {
               flat.quad(last, quad, 1.0F, 1.0F, 1.0F, 1.0F, light, overlay);
            }
         });
      }
   }

   private static boolean atlas(RenderLayer type) {
      return type == TexturedRenderLayers.getItemTranslucentCull()
         || type == TexturedRenderLayers.getBlockTranslucentCull()
         || type == TexturedRenderLayers.getEntityCutout()
         || type == TexturedRenderLayers.getEntitySolid();
   }

   public RenderCommandQueue getBatchingQueue(int order) {
      return this;
   }

   public void submitShadowPieces(MatrixStack matrices, float shadowRadius, List<ShadowPiece> shadowPieces) {
   }

   public void submitLabel(
      MatrixStack matrices,
      @Nullable Vec3d nameLabelPos,
      int y,
      Text label,
      boolean notSneaking,
      int light,
      double squaredDistanceToCamera,
      CameraRenderState cameraState
   ) {
   }

   public void submitText(
      MatrixStack matrices,
      float x,
      float y,
      OrderedText text,
      boolean dropShadow,
      TextLayerType layerType,
      int light,
      int color,
      int backgroundColor,
      int outlineColor
   ) {
   }

   public void submitFire(MatrixStack matrices, EntityRenderState renderState, Quaternionf rotation) {
   }

   public void submitLeash(MatrixStack matrices, LeashData leashData) {
   }

   public <S> void submitModel(
      Model<? super S> model,
      S state,
      MatrixStack matrices,
      RenderLayer renderLayer,
      int light,
      int overlay,
      int tintedColor,
      @Nullable Sprite sprite,
      int outlineColor,
      @Nullable CrumblingOverlayCommand crumblingOverlay
   ) {
   }

   public void submitModelPart(
      ModelPart part,
      MatrixStack matrices,
      RenderLayer renderLayer,
      int light,
      int overlay,
      @Nullable Sprite sprite,
      boolean sheeted,
      boolean hasGlint,
      int tintedColor,
      @Nullable CrumblingOverlayCommand crumblingOverlay,
      int outline
   ) {
   }

   public void submitBlock(MatrixStack matrices, BlockState state, int light, int overlay, int outlineColor) {
   }

   public void submitMovingBlock(MatrixStack matrices, MovingBlockRenderState state) {
   }

   public void submitBlockStateModel(
      MatrixStack matrices, RenderLayer renderLayer, BlockStateModel model, float r, float g, float b, int light, int overlay, int outlineColor
   ) {
   }

   public void submitCustom(MatrixStack matrices, RenderLayer renderLayer, Custom customRenderer) {
   }

   public void submitCustom(LayeredCustom customRenderer) {
   }

   private static final class Flat implements VertexConsumer {
      private final VertexConsumer target;
      private final int color;

      Flat(VertexConsumer target, int color) {
         this.target = target;
         this.color = color;
      }

      public VertexConsumer vertex(float x, float y, float z) {
         this.target.vertex(x, y, z);
         return this;
      }

      public VertexConsumer color(int red, int green, int blue, int alpha) {
         this.target.color(this.color);
         return this;
      }

      public VertexConsumer color(int argb) {
         this.target.color(this.color);
         return this;
      }

      public VertexConsumer texture(float u, float v) {
         this.target.texture(u, v);
         return this;
      }

      public VertexConsumer overlay(int u, int v) {
         return this;
      }

      public VertexConsumer light(int u, int v) {
         return this;
      }

      public VertexConsumer normal(float x, float y, float z) {
         return this;
      }

      public VertexConsumer lineWidth(float width) {
         return this;
      }
   }
}
