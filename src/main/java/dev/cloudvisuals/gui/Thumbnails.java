package dev.cloudvisuals.gui;

import dev.cloudvisuals.CloudVisualsClient;
import dev.cloudvisuals.render.ImageFiles;
import dev.cloudvisuals.render.gpu.CloudVisualsTexture;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.texture.NativeImage;

public final class Thumbnails {
   private static final Map<Path, Thumbnails.Thumbnail> CACHE = new HashMap<>();
   private static final Thumbnails.Thumbnail BROKEN = new Thumbnails.Thumbnail(null, 1, 1);

   private Thumbnails() {
   }

   public static Thumbnails.Thumbnail get(Path file) {
      Thumbnails.Thumbnail thumbnail = CACHE.computeIfAbsent(file, Thumbnails::load);
      return thumbnail == BROKEN ? null : thumbnail;
   }

   private static Thumbnails.Thumbnail load(Path file) {
      try {
         NativeImage image = ImageFiles.toNative(ImageFiles.read(file, 256));
         CloudVisualsTexture texture = new CloudVisualsTexture("thumbnail", image).filter(true, false);
         return new Thumbnails.Thumbnail(texture, image.getWidth(), image.getHeight());
      } catch (IOException | RuntimeException e) {
         CloudVisualsClient.LOGGER.warn("[Cloud Visuals] cannot read picture {}", file, e);
         return BROKEN;
      }
   }

   public record Thumbnail(CloudVisualsTexture texture, int width, int height) {
      public Thumbnail {
      }
   }
}
