package dev.cloudvisuals.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import dev.cloudvisuals.render.gpu.CloudVisualsShader;
import dev.cloudvisuals.render.gpu.Blend;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.LayeringTransform;
import net.minecraft.client.render.OutputTarget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.RenderSetup.OutlineMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

public final class CloudVisualsRenderTypes {
   private static final Map<Identifier, RenderLayer> GLOW_SPRITES = new HashMap<>();
   private static final Map<Identifier, RenderLayer> GLOW_SPRITES_THROUGH = new HashMap<>();
   private static final Map<Identifier, RenderLayer> SOFT_SPRITES = new HashMap<>();
   private static final Map<Identifier, RenderLayer> LIT = new HashMap<>();
   private static final OutputTarget GLOW_TARGET = new OutputTarget("cloudvisuals_glow_target", GlowEngine::glowTarget);
   private static final OutputTarget HALO_TARGET = new OutputTarget("cloudvisuals_halo_target", GlowEngine::haloTarget);
   private static final RenderPipeline GLOW_PIPELINE = RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_PROJECTION_FOG_SNIPPET})
      .withLocation(id("pipeline/glow"))
      .withVertexShader("core/rendertype_lightning")
      .withFragmentShader("core/rendertype_lightning")
      .withBlend(BlendFunction.LIGHTNING)
      .withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.QUADS)
      .withDepthWrite(false)
      .withCull(false)
      .build();
   private static final RenderPipeline GLOW_THROUGH_PIPELINE = RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_PROJECTION_FOG_SNIPPET})
      .withLocation(id("pipeline/glow_through"))
      .withVertexShader("core/rendertype_lightning")
      .withFragmentShader("core/rendertype_lightning")
      .withBlend(BlendFunction.LIGHTNING)
      .withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.QUADS)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withDepthWrite(false)
      .withCull(false)
      .build();
   private static final RenderPipeline FACES_PIPELINE = RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
      .withLocation(id("pipeline/faces"))
      .withVertexShader("core/position_color")
      .withFragmentShader("core/position_color")
      .withBlend(BlendFunction.TRANSLUCENT)
      .withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.QUADS)
      .withDepthWrite(false)
      .withCull(false)
      .build();
   public static final RenderLayer GLOW = RenderLayer.of(
      "cloudvisuals_glow", RenderSetup.builder(GLOW_PIPELINE).outputTarget(GLOW_TARGET).translucent().expectedBufferSize(8192).build()
   );
   public static final RenderLayer FACES = RenderLayer.of(
      "cloudvisuals_faces", RenderSetup.builder(FACES_PIPELINE).layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).translucent().build()
   );
   public static final RenderLayer ITEM_SILHOUETTE = silhouette("cloudvisuals_item_silhouette", SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
   public static final RenderLayer ITEM_SILHOUETTE_ITEMS = silhouette("cloudvisuals_item_silhouette_items", SpriteAtlasTexture.ITEMS_ATLAS_TEXTURE);
   public static final RenderLayer GLOW_THROUGH = RenderLayer.of(
      "cloudvisuals_glow_through", RenderSetup.builder(GLOW_THROUGH_PIPELINE).outputTarget(GLOW_TARGET).translucent().expectedBufferSize(2048).build()
   );
   private static final RenderPipeline ADDITIVE_ENTITY_PIPELINE = RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_PROJECTION_FOG_SNIPPET})
      .withLocation(id("pipeline/additive_entity"))
      .withVertexShader("core/entity")
      .withFragmentShader("core/entity")
      .withShaderDefine("EMISSIVE")
      .withShaderDefine("NO_OVERLAY")
      .withShaderDefine("NO_CARDINAL_LIGHTING")
      .withSampler("Sampler0")
      .withBlend(BlendFunction.ADDITIVE)
      .withDepthWrite(false)
      .withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, DrawMode.QUADS)
      .build();
   private static final Map<Identifier, RenderLayer> ADDITIVE_ENTITY = new HashMap<>();

   private CloudVisualsRenderTypes() {
   }

   private static Identifier id(String path) {
      return Identifier.of("cloudvisuals", path);
   }

   private static RenderLayer silhouette(String name, Identifier atlas) {
      return RenderLayer.of(
         name,
         RenderSetup.builder(Shaders.silhouette().pipeline(Blend.REPLACE, CloudVisualsShader.Depth.TEST))
            .texture("Sampler0", atlas)
            .outputTarget(HALO_TARGET)
            .expectedBufferSize(4096)
            .build()
      );
   }

   public static RenderLayer glowSpriteThrough(Identifier texture) {
      return GLOW_SPRITES_THROUGH.computeIfAbsent(
         texture,
         id -> RenderLayer.of(
            "cloudvisuals_glow_sprite_through",
            RenderSetup.builder(Shaders.softSprite().pipeline(Blend.LIGHTNING, CloudVisualsShader.Depth.NONE))
               .texture("Sampler0", id, CloudVisualsRenderTypes::smooth)
               .outputTarget(GLOW_TARGET)
               .translucent()
               .expectedBufferSize(1024)
               .build()
         )
      );
   }

   public static RenderLayer glowSprite(Identifier texture) {
      return GLOW_SPRITES.computeIfAbsent(
         texture,
         id -> RenderLayer.of(
            "cloudvisuals_glow_sprite",
            RenderSetup.builder(Shaders.softSprite().pipeline(Blend.LIGHTNING, CloudVisualsShader.Depth.TEST))
               .texture("Sampler0", id, CloudVisualsRenderTypes::smooth)
               .outputTarget(GLOW_TARGET)
               .translucent()
               .expectedBufferSize(4096)
               .build()
         )
      );
   }

   public static RenderLayer softSprite(Identifier texture) {
      return SOFT_SPRITES.computeIfAbsent(
         texture,
         id -> RenderLayer.of(
            "cloudvisuals_soft_sprite",
            RenderSetup.builder(Shaders.softSprite().pipeline(Blend.NORMAL, CloudVisualsShader.Depth.TEST))
               .texture("Sampler0", id, CloudVisualsRenderTypes::smooth)
               .outputTarget(OutputTarget.WEATHER_TARGET)
               .translucent()
               .expectedBufferSize(4096)
               .build()
         )
      );
   }

   public static RenderLayer lit(Identifier texture) {
      return LIT.computeIfAbsent(
         texture,
         id -> RenderLayer.of(
            "cloudvisuals_lit",
            RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
               .texture("Sampler0", id, CloudVisualsRenderTypes::smooth)
               .useLightmap()
               .useOverlay()
               .crumbling()
               .translucent()
               .expectedBufferSize(4096)
               .outlineMode(OutlineMode.AFFECTS_OUTLINE)
               .build()
         )
      );
   }

   public static RenderLayer lines() {
      return RenderLayers.lines();
   }

   public static RenderLayer additiveEntity(Identifier texture) {
      return ADDITIVE_ENTITY.computeIfAbsent(
         texture,
         id -> RenderLayer.of("cloudvisuals_additive_entity", RenderSetup.builder(ADDITIVE_ENTITY_PIPELINE).texture("Sampler0", id).translucent().build())
      );
   }

   private static GpuSampler smooth() {
      return RenderSystem.getSamplerCache().getRepeated(FilterMode.LINEAR);
   }
}
