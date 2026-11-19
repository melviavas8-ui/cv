package dev.cloudvisuals.render;

import com.mojang.blaze3d.textures.GpuTextureView;
import dev.cloudvisuals.render.gpu.CloudVisualsShader;
import dev.cloudvisuals.render.gpu.Blend;
import dev.cloudvisuals.render.gpu.GuiDraw;
import dev.cloudvisuals.render.style.Corners;
import dev.cloudvisuals.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public final class Images {
   private static final float[] RADII = new float[4];
   private static final float[] SHAPES = new float[4];

   private Images() {
   }

   public static void draw(
      DrawContext g, AbstractTexture texture, float u0, float v0, float u1, float v1, float x, float y, float w, float h, Corners corners, float alpha
   ) {
      draw(g, texture.getGlTextureView(), texture.getSampler(), u0, v0, u1, v1, x, y, w, h, corners, alpha);
   }

   public static void draw(
      DrawContext g,
      GpuTextureView view,
      GpuSampler sampler,
      float u0,
      float v0,
      float u1,
      float v1,
      float x,
      float y,
      float w,
      float h,
      Corners corners,
      float alpha
   ) {
      CloudVisualsShader shader = Shaders.roundedImage();
      if (!(w <= 0.0F) && !(h <= 0.0F) && !(alpha <= 0.001F)) {
         corners.writeRadii(RADII, 0);
         corners.writeShapes(SHAPES, 0);
         shader.safeGetUniform("Size").set(w, h);
         shader.safeGetUniform("Radius").set(RADII[0], RADII[1], RADII[2], RADII[3]);
         shader.safeGetUniform("Corner").set(SHAPES[0], SHAPES[1], SHAPES[2], SHAPES[3]);
         shader.safeGetUniform("UvRect").set(u0, v0, u1, v1);
         shader.setSampler("Sampler0", view, sampler);
         int color = ColorUtil.fade(-1, alpha);
         Matrix4f pose = GuiDraw.pose(g);
         BufferBuilder buffer = GuiDraw.begin(shader);
         buffer.vertex(pose, x, y, 0.0F).texture(0.0F, 0.0F).color(color);
         buffer.vertex(pose, x, y + h, 0.0F).texture(0.0F, h).color(color);
         buffer.vertex(pose, x + w, y + h, 0.0F).texture(w, h).color(color);
         buffer.vertex(pose, x + w, y, 0.0F).texture(w, 0.0F).color(color);
         GuiDraw.submit(g, shader, Blend.NORMAL, buffer);
      }
   }

   public static void playerFace(DrawContext g, Identifier skin, float x, float y, float size, Corners corners, float alpha) {
      AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(skin);
      draw(g, texture, 0.125F, 0.125F, 0.25F, 0.25F, x, y, size, size, corners, alpha);
      draw(g, texture, 0.625F, 0.125F, 0.75F, 0.25F, x, y, size, size, corners, alpha);
   }
}
