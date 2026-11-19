package dev.cloudvisuals.render;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.cloudvisuals.CloudVisualsClient;
import dev.cloudvisuals.compat.Compat;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.visual.ColorGradingModule;
import dev.cloudvisuals.module.impl.visual.MotionBlurModule;
import dev.cloudvisuals.render.gpu.CloudVisualsShader;
import dev.cloudvisuals.render.gpu.Blend;
import dev.cloudvisuals.render.gpu.ColorTarget;
import dev.cloudvisuals.render.gpu.GpuDraw;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

public final class PostEffects {
   private static ColorTarget scene;
   private static ColorTarget history;
   private static boolean historyValid;
   private static boolean broken;

   private PostEffects() {
   }

   public static void apply() {
      MinecraftClient mc = MinecraftClient.getInstance();
      ColorGradingModule grading = Modules.colorGrading();
      MotionBlurModule blur = Modules.motionBlur();
      boolean grade = grading != null && grading.isEnabled();
      float blurStrength = blur != null && blur.isEnabled() ? blur.strength() : 0.0F;
      CloudVisualsShader shader = Shaders.postGrade();
      if (!broken && mc.world != null && (grade || !(blurStrength <= 0.0F)) && !Compat.shaderPackActive() && shader.ready(Blend.REPLACE)) {
         try {
            render(mc.getFramebuffer(), shader, grading, grade, blurStrength);
         } catch (RuntimeException e) {
            broken = true;
            CloudVisualsClient.LOGGER.error("[Cloud Visuals] post effects disabled", e);
         }
      } else {
         historyValid = false;
      }
   }

   private static void render(Framebuffer main, CloudVisualsShader shader, ColorGradingModule grading, boolean grade, float blurStrength) {
      boolean resized = scene == null || scene.width() != main.textureWidth || scene.height() != main.textureHeight;
      scene = ColorTarget.ensure(scene, "post scene", main.textureWidth, main.textureHeight, false);
      history = ColorTarget.ensure(history, "post history", main.textureWidth, main.textureHeight, false);
      if (resized) {
         historyValid = false;
      }

      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
      encoder.copyTextureToTexture(main.getColorAttachment(), scene.texture(), 0, 0, 0, 0, 0, main.textureWidth, main.textureHeight);
      shader.setSampler("DiffuseSampler", scene.view(), ColorTarget.linear());
      shader.setSampler("HistorySampler", history.view(), ColorTarget.linear());
      shader.safeGetUniform("Grade").set(grade ? 1.0F : 0.0F);
      if (grade) {
         shader.safeGetUniform("Saturation").set(grading.saturation());
         shader.safeGetUniform("Contrast").set(grading.contrast());
         shader.safeGetUniform("Vibrance").set(grading.vibrance());
         shader.safeGetUniform("Exposure").set(grading.exposure());
         shader.safeGetUniform("Warmth").set(grading.warmth());
      }

      shader.safeGetUniform("Blur").set(historyValid ? blurStrength : 0.0F);
      GpuDraw.screen(shader, Blend.REPLACE, main.getColorAttachmentView(), false);
      if (blurStrength > 0.0F) {
         encoder.copyTextureToTexture(main.getColorAttachment(), history.texture(), 0, 0, 0, 0, 0, main.textureWidth, main.textureHeight);
         historyValid = true;
      } else {
         historyValid = false;
      }
   }
}
