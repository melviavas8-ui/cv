package dev.cloudvisuals.render.gpu;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record QuadState(Matrix3x2f pose, float x0, float y0, float x1, float y1, int color, @Nullable ScreenRect scissorArea, @Nullable ScreenRect bounds)
   implements SimpleGuiElementRenderState {
   public QuadState {
   }

   public static void submit(DrawContext g, float x0, float y0, float x1, float y1, int color) {
      Matrix3x2f pose = new Matrix3x2f(g.getMatrices());
      ScreenRect scissor = g.scissorStack.peekLast();
      int left = (int)Math.floor(x0);
      int top = (int)Math.floor(y0);
      ScreenRect rect = new ScreenRect(left, top, Math.max(1, (int)Math.ceil(x1) - left), Math.max(1, (int)Math.ceil(y1) - top)).transformEachVertex(pose);
      ScreenRect bounds = scissor != null ? scissor.intersection(rect) : rect;
      if (bounds != null) {
         g.state.addSimpleElement(new QuadState(pose, x0, y0, x1, y1, color, scissor, bounds));
      }
   }

   public void setupVertices(VertexConsumer vertices) {
      vertices.vertex(this.pose, this.x0, this.y0).color(this.color);
      vertices.vertex(this.pose, this.x0, this.y1).color(this.color);
      vertices.vertex(this.pose, this.x1, this.y1).color(this.color);
      vertices.vertex(this.pose, this.x1, this.y0).color(this.color);
   }

   public RenderPipeline pipeline() {
      return RenderPipelines.GUI;
   }

   public TextureSetup textureSetup() {
      return TextureSetup.empty();
   }
}
