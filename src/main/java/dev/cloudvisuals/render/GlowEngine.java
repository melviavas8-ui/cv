package dev.cloudvisuals.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.cloudvisuals.CloudVisualsClient;
import dev.cloudvisuals.compat.Compat;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.visual.BloomModule;
import dev.cloudvisuals.render.gpu.CloudVisualsShader;
import dev.cloudvisuals.render.gpu.Blend;
import dev.cloudvisuals.render.gpu.ColorTarget;
import dev.cloudvisuals.render.gpu.GpuDraw;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.jetbrains.annotations.Nullable;

public final class GlowEngine {
   private static final int LEVELS = 5;
   private static SimpleFramebuffer glow;
   private static SimpleFramebuffer halo;
   private static final ColorTarget[] down = new ColorTarget[6];
   private static final ColorTarget[] scratch = new ColorTarget[12];
   private static boolean worldPhase;
   private static boolean used;
   private static boolean haloUsed;
   private static boolean depthFresh;
   private static boolean haloDepthFresh;
   private static boolean broken;

   private GlowEngine() {
   }

   private static BloomModule module() {
      BloomModule module = Modules.bloom();
      return module != null && module.isEnabled() ? module : null;
   }

   public static boolean active() {
      return worldPhase
         && !broken
         && module() != null
         && !Compat.shaderPackActive()
         && Shaders.glowComposite().ready(Blend.ADDITIVE_PREMULTIPLIED)
         && Shaders.kawaseDown().ready(Blend.REPLACE)
         && Shaders.kawaseUp().ready(Blend.REPLACE);
   }

   public static void beginWorld() {
      worldPhase = true;
      haloUsed = false;
      used = false;
      haloDepthFresh = false;
      depthFresh = false;
   }

   public static void refreshDepth() {
      haloDepthFresh = false;
      depthFresh = false;
   }

   @Nullable
   public static Framebuffer glowTarget() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (!active()) {
         return mc.worldRenderer.getWeatherFramebuffer();
      }

      try {
         glow = prepare(glow, "glow", !used, !depthFresh);
         depthFresh = true;
         used = true;
         return glow;
      } catch (RuntimeException e) {
         fail(e);
         return mc.worldRenderer.getWeatherFramebuffer();
      }
   }

   @Nullable
   public static Framebuffer haloTarget() {
      if (!active()) {
         return null;
      }

      try {
         halo = prepare(halo, "halo", !haloUsed, !haloDepthFresh);
         haloDepthFresh = true;
         haloUsed = true;
         return halo;
      } catch (RuntimeException e) {
         fail(e);
         return null;
      }
   }

   private static SimpleFramebuffer prepare(SimpleFramebuffer target, String label, boolean clear, boolean copyDepth) {
      Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
      SimpleFramebuffer result = target;
      if (result == null) {
         result = new SimpleFramebuffer("Cloud Visuals " + label, main.textureWidth, main.textureHeight, true);
      } else if (result.textureWidth != main.textureWidth || result.textureHeight != main.textureHeight) {
         result.resize(main.textureWidth, main.textureHeight);
      }

      if (clear) {
         RenderSystem.getDevice().createCommandEncoder().clearColorTexture(result.getColorAttachment(), 0);
      }

      if (copyDepth) {
         result.copyDepthFrom(main);
      }

      return result;
   }

   public static void endWorld() {
      boolean sharp = used;
      boolean soft = haloUsed;
      boolean draw = (sharp || soft) && active();
      worldPhase = false;
      haloUsed = false;
      used = false;
      if (draw) {
         BloomModule settings = module();
         Framebuffer main = MinecraftClient.getInstance().getFramebuffer();

         try {
            SimpleFramebuffer first = sharp ? glow : halo;
            int width = first.textureWidth;
            int height = first.textureHeight;
            GpuTextureView source = first.getColorAttachmentView();
            int sourceWidth = width;
            int sourceHeight = height;
            float spread = settings.spread();

            for (int i = 1; i <= 5; i++) {
               down[i] = ColorTarget.ensure(down[i], "glow down " + i, width >> i, height >> i, true);
               blur(Shaders.kawaseDown(), source, sourceWidth, sourceHeight, down[i], spread, Blend.REPLACE);
               if (i == 1 && sharp && soft) {
                  blur(Shaders.kawaseDown(), halo.getColorAttachmentView(), width, height, down[1], spread, Blend.ADDITIVE_PREMULTIPLIED);
               }

               source = down[i].view();
               sourceWidth = down[i].width();
               sourceHeight = down[i].height();
            }

            ColorTarget tight = rise(3, 0, spread, width, height);
            ColorTarget wide = rise(5, 1, spread, width, height);
            float strength = settings.strength();
            GpuTextureView mask = soft ? halo.getColorAttachmentView() : first.getColorAttachmentView();
            float cut = soft ? 0.9F : 0.0F;
            GpuTextureView out = main.getColorAttachmentView();
            if (sharp) {
               composite(out, glow.getColorAttachmentView(), 1.0F, 1.0F, mask, cut);
            }

            composite(out, tight.view(), 0.85F * strength, settings.saturation(), mask, cut);
            composite(out, wide.view(), 0.7F * strength * settings.halo(), settings.saturation(), mask, cut);
         } catch (RuntimeException e) {
            fail(e);
         }
      }
   }

   private static ColorTarget rise(int from, int set, float spread, int width, int height) {
      ColorTarget source = down[from];

      for (int level = from - 1; level >= 1; level--) {
         int slot = set * 5 + level;
         ColorTarget target = scratch[slot] = ColorTarget.ensure(scratch[slot], "glow up " + slot, width >> level, height >> level, true);
         blur(Shaders.kawaseUp(), source.view(), source.width(), source.height(), target, spread, Blend.REPLACE);
         source = target;
      }

      return source;
   }

   private static void blur(CloudVisualsShader shader, GpuTextureView texture, int sourceWidth, int sourceHeight, ColorTarget target, float spread, Blend blend) {
      shader.setSampler("DiffuseSampler", texture, ColorTarget.linear());
      shader.safeGetUniform("HalfPixel").set(0.5F / sourceWidth, 0.5F / sourceHeight);
      shader.safeGetUniform("Offset").set(spread);
      shader.safeGetUniform("Linearize").set(0.0F);
      GpuDraw.screen(shader, blend, target.view(), false);
   }

   private static void composite(GpuTextureView out, GpuTextureView texture, float strength, float saturation, GpuTextureView mask, float cut) {
      if (!(strength <= 0.001F)) {
         CloudVisualsShader shader = Shaders.glowComposite();
         shader.setSampler("DiffuseSampler", texture, ColorTarget.linear());
         shader.setSampler("MaskSampler", mask, ColorTarget.linear());
         shader.safeGetUniform("Cut").set(cut);
         shader.safeGetUniform("Strength").set(strength);
         shader.safeGetUniform("Saturation").set(saturation);
         GpuDraw.screen(shader, Blend.ADDITIVE_PREMULTIPLIED, out, false);
      }
   }

   private static void fail(RuntimeException e) {
      broken = true;
      CloudVisualsClient.LOGGER.error("[Cloud Visuals] glow engine disabled", e);
   }
}
