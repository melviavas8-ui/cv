package dev.cloudvisuals.render;

import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import dev.cloudvisuals.render.gpu.CloudVisualsShader;
import net.minecraft.client.render.VertexFormats;

public final class Shaders {
   private static final DrawMode QUADS = DrawMode.QUADS;
   private static final CloudVisualsShader SURFACE = CloudVisualsShader.create("surface", VertexFormats.POSITION_TEXTURE_COLOR, QUADS);
   private static final CloudVisualsShader LIQUID_GLASS = CloudVisualsShader.create("liquid_glass", VertexFormats.POSITION_TEXTURE_COLOR, QUADS);
   private static final CloudVisualsShader TEXT = CloudVisualsShader.create("sdf_text", VertexFormats.POSITION_TEXTURE_COLOR, QUADS);
   private static final CloudVisualsShader KAWASE_DOWN = CloudVisualsShader.create("kawase_down", VertexFormats.POSITION, QUADS);
   private static final CloudVisualsShader KAWASE_UP = CloudVisualsShader.create("kawase_up", VertexFormats.POSITION, QUADS);
   private static final CloudVisualsShader ROUNDED_IMAGE = CloudVisualsShader.create("rounded_image", VertexFormats.POSITION_TEXTURE_COLOR, QUADS);
   private static final CloudVisualsShader CHARACTER = CloudVisualsShader.create("character", VertexFormats.POSITION_TEXTURE_COLOR, QUADS);
   private static final CloudVisualsShader POST_GRADE = CloudVisualsShader.create("post_grade", VertexFormats.POSITION, QUADS);
   private static final CloudVisualsShader SOFT_SPRITE = CloudVisualsShader.create("soft_sprite", VertexFormats.POSITION_TEXTURE_COLOR, QUADS);
   private static final CloudVisualsShader GLOW_COMPOSITE = CloudVisualsShader.create("glow_composite", VertexFormats.POSITION, QUADS);
   private static final CloudVisualsShader SILHOUETTE = CloudVisualsShader.create("silhouette", VertexFormats.POSITION_TEXTURE_COLOR, QUADS);
   private static final CloudVisualsShader BLACK_HOLE = CloudVisualsShader.create("black_hole", VertexFormats.POSITION_TEXTURE_COLOR, QUADS);
   private static final CloudVisualsShader NEBULA = CloudVisualsShader.create("nebula", VertexFormats.POSITION_TEXTURE_COLOR, QUADS);

   private Shaders() {
   }

   public static void register() {
   }

   public static CloudVisualsShader softSprite() {
      return SOFT_SPRITE;
   }

   public static CloudVisualsShader silhouette() {
      return SILHOUETTE;
   }

   public static CloudVisualsShader glowComposite() {
      return GLOW_COMPOSITE;
   }

   public static CloudVisualsShader surface() {
      return SURFACE;
   }

   public static CloudVisualsShader liquidGlass() {
      return LIQUID_GLASS;
   }

   public static CloudVisualsShader text() {
      return TEXT;
   }

   public static CloudVisualsShader kawaseDown() {
      return KAWASE_DOWN;
   }

   public static CloudVisualsShader character() {
      return CHARACTER;
   }

   public static CloudVisualsShader roundedImage() {
      return ROUNDED_IMAGE;
   }

   public static CloudVisualsShader postGrade() {
      return POST_GRADE;
   }

   public static CloudVisualsShader kawaseUp() {
      return KAWASE_UP;
   }

   public static CloudVisualsShader blackHole() {
      return BLACK_HOLE;
   }

   public static CloudVisualsShader nebula() {
      return NEBULA;
   }
}
