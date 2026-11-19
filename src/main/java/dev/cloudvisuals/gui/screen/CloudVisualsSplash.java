package dev.cloudvisuals.gui.screen;

import dev.cloudvisuals.module.impl.ScreensModule;
import dev.cloudvisuals.render.DropletLogo;
import dev.cloudvisuals.render.font.BitmapText;
import net.minecraft.client.gui.DrawContext;

public final class CloudVisualsSplash {
   public static final int BACKGROUND = -15987696;
   private static final DropletLogo LOGO = new DropletLogo("splash");
   private static final BitmapText WORDMARK = new BitmapText("unbounded-bold", "splash");
   private static boolean failed;

   private CloudVisualsSplash() {
   }

   public static boolean active() {
      return !failed && ScreensModule.loadingScreen();
   }

   public static void draw(DrawContext g, float alpha, float progress) {
      try {
         int width = g.getScaledWindowWidth();
         int height = g.getScaledWindowHeight();
         float logoW = Math.clamp(Math.min(width * 0.1F, height * 0.16F), 22.0F, 84.0F);
         float cloudW = logoW * 1.7F;
         float logoH = cloudW * DropletLogo.ASPECT;
         float textSize = logoW * 0.3F;
         float blockH = logoH + logoW * 0.34F + textSize;
         float top = height * 0.47F - blockH / 2.0F;
         float level = 0.08F + 0.92F * Math.clamp(progress, 0.0F, 1.0F);
         LOGO.draw(g, width / 2.0F - cloudW / 2.0F, top, cloudW, level, alpha);
         WORDMARK.drawCentered(g, "CLOUD VISUALS", width / 2.0F, top + logoH + logoW * 0.34F, textSize, 0.3F, -855307, alpha);
      } catch (RuntimeException e) {
         failed = true;
         ScreensModule.crashed(e);
      }
   }
}
