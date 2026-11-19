package dev.cloudvisuals.render;

import dev.cloudvisuals.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * Логотип-облако: анимация из спрайт-листа 3x3 (9 кадров по 0.12 с).
 * Два размера листа: большой для крупных логотипов и маленький для мелких, чтобы не было мерцания при уменьшении.
 * Файлы: assets/cloudvisuals/textures/gui/cloud_big.png и cloud_small.png.
 */
public final class DropletLogo {
   private static final int COLUMNS = 3;
   private static final int FRAMES = 9;
   private static final float FRAME_SECONDS = 0.12F;
   private static final int BIG_W = 418;
   private static final int BIG_H = 248;
   private static final int SMALL_W = 140;
   private static final int SMALL_H = 83;
   private static final float SMALL_LIMIT = 220.0F;
   private static final float DIM = 0.28F;
   private static final Identifier BIG = Identifier.of("cloudvisuals", "textures/gui/cloud_big.png");
   private static final Identifier SMALL = Identifier.of("cloudvisuals", "textures/gui/cloud_small.png");

   /** Высота облака относительно его ширины. */
   public static final float ASPECT = BIG_H / (float)BIG_W;

   public DropletLogo(String name) {
   }

   /** Облако с заполнением по прогрессу: бледное целиком, яркое снизу до уровня level (0..1). */
   public void draw(DrawContext g, float x, float y, float width, float level, float alpha) {
      if (!(alpha <= 0.003F) && !(width <= 1.0F)) {
         float fill = Math.clamp(level, 0.0F, 1.0F);
         if (fill >= 0.999F) {
            this.paint(g, x, y, width, alpha);
         } else {
            float height = width * ASPECT;
            this.paint(g, x, y, width, alpha * DIM);
            g.enableScissor(Math.round(x) - 1, Math.round(y + height * (1.0F - fill)), Math.round(x + width) + 1, Math.round(y + height) + 2);
            this.paint(g, x, y, width, alpha);
            g.disableScissor();
         }
      }
   }

   /** Облако целиком, без заполнения. */
   public void drawStatic(DrawContext g, float x, float y, float width, float alpha) {
      if (!(alpha <= 0.003F) && !(width <= 1.0F)) {
         this.paint(g, x, y, width, alpha);
      }
   }

   public void close() {
   }

   private void paint(DrawContext g, float x, float y, float width, float alpha) {
      double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
      boolean small = width * scale < SMALL_LIMIT;
      Identifier id = small ? SMALL : BIG;
      int cw = small ? SMALL_W : BIG_W;
      int ch = small ? SMALL_H : BIG_H;
      int frame = Math.floorMod((int)(Theme.time() / FRAME_SECONDS), FRAMES);
      float u = (frame % COLUMNS) * cw;
      float v = (frame / COLUMNS) * ch;
      float k = width / cw;
      g.getMatrices().pushMatrix();
      g.getMatrices().translate(x, y);
      g.getMatrices().scale(k, k);
      g.drawTexture(RenderPipelines.GUI_TEXTURED, id, 0, 0, u, v, cw, ch, cw, ch, cw * COLUMNS, ch * COLUMNS, ColorUtil.fade(-1, alpha));
      g.getMatrices().popMatrix();
   }
}
