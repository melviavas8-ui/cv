package dev.cloudvisuals.render;

import dev.cloudvisuals.CloudVisualsClient;
import dev.cloudvisuals.anim.Spring;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.ClickGuiModule;
import dev.cloudvisuals.render.gpu.CloudVisualsTexture;
import dev.cloudvisuals.render.style.Corners;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

public final class MenuBackground {
   private static final int MAX_SIZE = 2560;
   private static final Identifier ID = Identifier.of("cloudvisuals", "dynamic/menu_background");
   private static String loadedKey;
   private static CompletableFuture<NativeImage> loading;
   private static String loadingKey;
   private static CloudVisualsTexture texture;
   private static int width;
   private static int height;
   private static final Spring shiftX = new Spring(0.0, 1.2, 1.0);
   private static final Spring shiftY = new Spring(0.0, 1.2, 1.0);

   private MenuBackground() {
   }

   public static boolean active() {
      ClickGuiModule settings = Modules.clickGui();
      return settings != null && settings.customBackground() && settings.backgroundImage() != null;
   }

   public static boolean everywhere() {
      ClickGuiModule settings = Modules.clickGui();
      return active() && settings.backgroundInMenus();
   }

   public static boolean draw(DrawContext g, int screenWidth, int screenHeight, float alpha) {
      ClickGuiModule settings = Modules.clickGui();
      Path file = settings == null ? null : settings.backgroundImage();
      if (file == null) {
         return false;
      }

      int blur = Math.round(settings.backgroundBlur());

      long modified;
      try {
         modified = Files.getLastModifiedTime(file).toMillis();
      } catch (IOException e) {
         return false;
      }

      String key = file.toAbsolutePath() + "|" + modified + "|" + blur;
      if (!key.equals(loadedKey) && !key.equals(loadingKey)) {
         loadingKey = key;
         loading = CompletableFuture.supplyAsync(() -> decode(file, blur));
      }

      if (loading != null && loading.isDone() && key.equals(loadingKey)) {
         NativeImage image = loading.getNow(null);
         loading = null;
         loadingKey = null;
         loadedKey = key;
         if (image != null) {
            upload(image);
         } else if (texture != null) {
            release();
         }
      }

      return texture != null && draw(g, screenWidth, screenHeight, alpha, settings.backgroundParallax());
   }

   private static boolean draw(DrawContext g, int screenWidth, int screenHeight, float alpha, boolean parallax) {
      MinecraftClient mc = MinecraftClient.getInstance();
      float zoom = parallax ? 1.06F : 1.0F;
      float scale = Math.max((float)screenWidth / width, (float)screenHeight / height) * zoom;
      float w = width * scale;
      float h = height * scale;
      float mx = (float)(mc.mouse.getX() / Math.max(1, mc.getWindow().getWidth())) - 0.5F;
      float my = (float)(mc.mouse.getY() / Math.max(1, mc.getWindow().getHeight())) - 0.5F;
      float ox = parallax ? shiftX.setTarget(-mx).getFloat() : 0.0F;
      float oy = parallax ? shiftY.setTarget(-my).getFloat() : 0.0F;
      float x = (screenWidth - w) / 2.0F + ox * (w - screenWidth);
      float y = (screenHeight - h) / 2.0F + oy * (h - screenHeight);
      Images.draw(g, texture, 0.0F, 0.0F, 1.0F, 1.0F, x, y, w, h, Corners.NONE, alpha);
      return true;
   }

   private static NativeImage decode(Path file, int blur) {
      try {
         ImageFiles.Pixels pixels = ImageFiles.read(file, 2560);
         if (blur > 0) {
            int factor = Math.max(2, Math.min(8, Math.max(pixels.width(), pixels.height()) / 480));
            pixels = ImageFiles.blur(ImageFiles.downscale(pixels, factor), Math.max(1, blur * 12 / 100));
         }

         return ImageFiles.toNative(pixels);
      } catch (IOException | RuntimeException e) {
         CloudVisualsClient.LOGGER.warn("[Cloud Visuals] cannot read menu background {}", file, e);
         return null;
      }
   }

   private static void upload(NativeImage image) {
      release();
      texture = new CloudVisualsTexture("menu background", image).filter(true, false);
      width = image.getWidth();
      height = image.getHeight();
      MinecraftClient.getInstance().getTextureManager().registerTexture(ID, texture);
   }

   private static void release() {
      if (texture != null) {
         MinecraftClient.getInstance().getTextureManager().destroyTexture(ID);
      }

      texture = null;
   }
}
