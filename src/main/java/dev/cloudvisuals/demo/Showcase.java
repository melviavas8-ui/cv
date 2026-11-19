package dev.cloudvisuals.demo;

import dev.cloudvisuals.render.Backdrop;
import dev.cloudvisuals.render.LiquidGlass;
import dev.cloudvisuals.render.Render2D;
import dev.cloudvisuals.render.Surfaces;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.font.FontWeight;
import dev.cloudvisuals.render.font.Fonts;
import dev.cloudvisuals.render.font.SdfFont;
import dev.cloudvisuals.render.style.Corner;
import dev.cloudvisuals.render.style.Corners;
import dev.cloudvisuals.render.style.Glass;
import dev.cloudvisuals.render.style.Paint;
import dev.cloudvisuals.render.style.Shadow;
import dev.cloudvisuals.render.style.Surface;
import dev.cloudvisuals.ui.GlassSlider;
import dev.cloudvisuals.ui.SegmentedControl;
import dev.cloudvisuals.ui.ToggleSwitch;
import java.util.Random;
import net.minecraft.client.gui.DrawContext;

final class Showcase {
   private static final float TILE_W = 92.0F;
   private static final float TILE_H = 58.0F;
   private static final float GLASS_H = 70.0F;
   private static final float GAP = 10.0F;
   private static final String[] CORNER_NAMES = new String[]{
      "\u041a\u0440\u0443\u0433",
      "\u0421\u043a\u0432\u0438\u0440\u043a\u043b iOS",
      "\u0421\u0440\u0435\u0437",
      "\u0412\u043e\u0433\u043d\u0443\u0442\u044b\u0435",
      "\u041e\u0441\u0442\u0440\u044b\u0435",
      "\u0412\u0441\u0435 \u0440\u0430\u0437\u043d\u044b\u0435"
   };
   private static final Corners[] CORNERS = new Corners[]{
      Corners.round(16.0F),
      Corners.all(Corner.squircle(22.0F)),
      Corners.all(Corner.bevel(14.0F)),
      Corners.all(Corner.scoop(14.0F)),
      Corners.NONE,
      new Corners(Corner.round(24.0F), Corner.bevel(12.0F), Corner.scoop(12.0F), Corner.NONE)
   };
   private static final String[] PAINT_NAMES = new String[]{
      "\u041b\u0438\u043d\u0435\u0439\u043d\u044b\u0439",
      "\u0420\u0430\u0434\u0438\u0430\u043b\u044c\u043d\u044b\u0439",
      "\u0423\u0433\u043b\u043e\u0432\u0430\u044f \u043e\u0431\u0432\u043e\u0434\u043a\u0430",
      "\u041d\u0435\u043e\u043d",
      "\u0412\u043d\u0443\u0442\u0440\u0435\u043d\u043d\u044f\u044f \u0442\u0435\u043d\u044c",
      "4 \u0446\u0432\u0435\u0442\u0430 \u00b7 OKLab"
   };
   private static final Surface[] PAINTS = createPaintSurfaces();
   private static final String[] GLASS_NAMES = new String[]{
      "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0435",
      "\u041c\u0430\u0442\u043e\u0432\u043e\u0435",
      "\u0426\u0432\u0435\u0442\u043d\u043e\u0435",
      "\u0416\u0438\u0434\u043a\u043e\u0441\u0442\u044c",
      "\u041b\u0438\u043d\u0437\u0430",
      "\u041a\u0430\u043f\u043b\u0438"
   };
   private static final Glass CLEAR = Glass.clear();
   private static final Glass FROSTED = Glass.dark();
   private static final Glass TINTED = Glass.clear();
   private static final Glass LIQUID = Glass.clear();
   private static final Glass LENS = Glass.clear();
   private static final Glass DROPS = Glass.lens();
   private static final ToggleSwitch toggle = new ToggleSwitch(true);
   private static final SegmentedControl segments = new SegmentedControl(
      "\u0421\u0442\u0435\u043a\u043b\u043e", "\u0421\u0432\u0435\u0442\u043b\u043e\u0435", "\u0422\u0451\u043c\u043d\u043e\u0435"
   );
   private static final GlassSlider slider = new GlassSlider(0.6F);
   private static final Random random = new Random(3L);
   private static long nextToggle;
   private static long nextSegment;
   private static long nextSlider;

   private Showcase() {
   }

   static void draw(DrawContext g, int screenWidth, int screenHeight) {
      SdfFont bold = Fonts.get(FontWeight.SEMIBOLD);
      SdfFont regular = Fonts.get(FontWeight.REGULAR);
      if (bold != null && regular != null) {
         autoplay();
         Backdrop.capture();
         float step = 102.0F;
         float left = (screenWidth - 602.0F) / 2.0F;
         text(g, bold, "CloudVisuals \u2014 \u0432\u0438\u0442\u0440\u0438\u043d\u0430 \u0433\u0440\u0430\u0444\u0438\u043a\u0438", left, 10.0F, 11.0F);
         text(
            g,
            regular,
            "\u0414\u0435\u043c\u043e\u043d\u0441\u0442\u0440\u0430\u0446\u0438\u044f: \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u044b \u043f\u0435\u0440\u0435\u043a\u043b\u044e\u0447\u0430\u044e\u0442\u0441\u044f \u0441\u0430\u043c\u0438 \u00b7 Esc \u2014 \u0432\u0435\u0440\u043d\u0443\u0442\u044c\u0441\u044f \u0432 \u043c\u0435\u043d\u044e",
            left,
            24.0F,
            7.0F
         );
         float row1 = 48.0F;
         text(g, bold, "\u0424\u043e\u0440\u043c\u0430 \u0443\u0433\u043b\u043e\u0432", left, row1 - 11.0F, 7.5F);

         for (int i = 0; i < CORNERS.length; i++) {
            Surface surface = new Surface();
            surface.corners = CORNERS[i];
            surface.fill = Paint.linear(135.0F, -10593050, -4236558);
            surface.shadow = Shadow.soft(1610612736, 6.0F, 3.0F);
            surface.innerShadow = new Shadow(1358954495, 2.5F, 0.0F, 0.0F, 1.5F);
            surface.border = Paint.solid(822083583);
            surface.borderWidth = 0.8F;
            float x = left + i * step;
            Surfaces.draw(g, x, row1, 92.0F, 58.0F, surface, 1.0F);
            caption(g, regular, CORNER_NAMES[i], x, row1 + 58.0F + 3.0F);
         }

         float row2 = row1 + 58.0F + 26.0F;
         text(g, bold, "\u0417\u0430\u043b\u0438\u0432\u043a\u0438, \u0441\u0432\u0435\u0442 \u0438 \u0442\u0435\u043d\u0438", left, row2 - 11.0F, 7.5F);

         for (int i = 0; i < PAINTS.length; i++) {
            float x = left + i * step;
            Surfaces.draw(g, x, row2, 92.0F, 58.0F, PAINTS[i], 1.0F);
            caption(g, regular, PAINT_NAMES[i], x, row2 + 58.0F + 3.0F);
         }

         float row3 = row2 + 58.0F + 26.0F;
         text(g, bold, "\u0416\u0438\u0434\u043a\u043e\u0435 \u0441\u0442\u0435\u043a\u043b\u043e", left, row3 - 11.0F, 7.5F);
         Glass[] glasses = new Glass[]{CLEAR, FROSTED, TINTED, LIQUID};
         Corners glassCorners = Corners.all(Corner.squircle(20.0F));

         for (int i = 0; i < glasses.length; i++) {
            float x = left + i * step;
            Surfaces.shadow(g, x, row3, 92.0F, 70.0F, glassCorners, Shadow.soft(1342177280, 7.0F, 4.0F), 1.0F);
            LiquidGlass.draw(g, x, row3, 92.0F, 70.0F, glassCorners, glasses[i], 1.0F);
            caption(g, regular, GLASS_NAMES[i], x, row3 + 70.0F + 3.0F);
         }

         float lensX = left + 4.0F * step;
         float size = 64.0F;
         float lensCx = lensX + 46.0F;
         float lensCy = row3 + 35.0F;
         Surfaces.shadow(g, lensCx - size / 2.0F, lensCy - size / 2.0F, size, size, Corners.round(size / 2.0F), Shadow.soft(1342177280, 7.0F, 4.0F), 1.0F);
         LiquidGlass.draw(g, lensCx - size / 2.0F, lensCy - size / 2.0F, size, size, Corners.round(size / 2.0F), LENS, 1.0F);
         caption(g, regular, GLASS_NAMES[4], lensX, row3 + 70.0F + 3.0F);
         float dropsX = left + 5.0F * step;
         float t = Theme.time() * 1.6F;
         float radius = 17.0F;
         float cx = dropsX + 46.0F;
         float cy = row3 + 35.0F;
         float dx = (float)Math.cos(t) * 15.0F;
         float dy = (float)Math.sin(t * 0.8F) * 9.0F;
         LiquidGlass.shapes()
            .add(cx + dx - radius, cy + dy - radius, radius * 2.0F, radius * 2.0F, Corners.round(radius))
            .add(cx - dx - radius, cy - dy - radius, radius * 2.0F, radius * 2.0F, Corners.round(radius))
            .draw(g, DROPS, 16.0F, 1.0F);
         caption(g, regular, GLASS_NAMES[5], dropsX, row3 + 70.0F + 3.0F);
         Glass theme = Theme.glass();
         float barW = 420.0F;
         float barH = 34.0F;
         float barX = (screenWidth - barW) / 2.0F;
         float barY = row3 + 70.0F + 18.0F;
         Render2D.shadow(g, barX, barY + 3.0F, barW, barH, 12.0F, 12.0F, 1342177280);
         LiquidGlass.draw(g, barX, barY, barW, barH, 12.0F, theme, 1.0F);
         toggle.render(g, barX + 12.0F, barY + 10.0F, 26.0F, 14.0F, theme, 1.0F);
         segments.render(g, barX + 50.0F, barY + 8.0F, 190.0F, 18.0F, theme, 1.0F);
         slider.render(g, barX + 252.0F, barY + 11.0F, 156.0F, 12.0F, theme, 1.0F);
      }
   }

   private static Surface[] createPaintSurfaces() {
      Corners corners = Corners.all(Corner.squircle(16.0F));
      Surface linear = surface(corners);
      linear.fill = Paint.linear(0.0F, -51361, -24822, -10742).spinning(45.0F);
      Surface radial = surface(corners);
      radial.fill = Paint.radial(0.3F, 0.25F, 1.4F, -10169601, -16087809, -14934930);
      Surface conic = surface(corners);
      conic.fill = Paint.solid(-15724524);
      conic.border = Paint.conic(0.0F, -53867, -13580801, -10742, -53867).spinning(120.0F);
      conic.borderWidth = 2.5F;
      conic.glow = Shadow.glow(-1866507534, 8.0F, 0.0F);
      Surface neon = surface(corners);
      neon.fill = Paint.solid(-16053486);
      neon.glow = Shadow.glow(-1070545409, 10.0F, 1.0F);
      neon.border = Paint.linear(90.0F, -13580801, -10593050);
      neon.borderWidth = 1.5F;
      Surface inner = surface(corners);
      inner.fill = Paint.solid(-1710614);
      inner.innerShadow = new Shadow(1426063360, 5.0F, 0.0F, 3.0F, 3.0F);
      inner.shadow = new Shadow(1073741824, 8.0F, 0.0F, 3.0F, 4.0F);
      Surface oklab = surface(corners);
      oklab.fill = Paint.linear(0.0F, -16087809, -13577896, -10742, -47814);
      return new Surface[]{linear, radial, conic, neon, inner, oklab};
   }

   private static Surface surface(Corners corners) {
      Surface surface = new Surface();
      surface.corners = corners;
      surface.shadow = Shadow.soft(1610612736, 6.0F, 3.0F);
      return surface;
   }

   private static void autoplay() {
      long now = System.currentTimeMillis();
      if (nextToggle == 0L) {
         nextToggle = now + 1200L;
         nextSegment = now + 2000L;
         nextSlider = now + 800L;
      }

      if (now >= nextToggle) {
         toggle.set(!toggle.isOn());
         nextToggle = now + 1700L;
      }

      if (now >= nextSegment) {
         segments.select((segments.selected() + 1) % 3);
         nextSegment = now + 2300L;
      }

      if (now >= nextSlider) {
         slider.set(0.1F + random.nextFloat() * 0.8F);
         nextSlider = now + 1300L;
      }
   }

   private static void text(DrawContext g, SdfFont font, String text, float x, float y, float size) {
      font.draw(g, text, x + 0.5F, y + 0.6F, size, -1879048192);
      font.draw(g, text, x, y, size, -1);
   }

   private static void caption(DrawContext g, SdfFont font, String text, float tileX, float y) {
      float size = 6.5F;
      text(g, font, text, tileX + (92.0F - font.width(text, size)) / 2.0F, y, size);
   }

   static {
      CLEAR.refraction = 12.0F;
      CLEAR.dispersion = 0.5F;
      FROSTED.blur = 1.0F;
      TINTED.blur = 0.55F;
      TINTED.rim = 0.5F;
      TINTED.tint = Paint.linear(135.0F, 1895771541, 1882244607);
      LIQUID.blur = 0.12F;
      LIQUID.wobble = 3.5F;
      LIQUID.wobbleScale = 1.4F;
      LIQUID.wobbleSpeed = 1.2F;
      LIQUID.dispersion = 0.3F;
      LENS.blur = 0.0F;
      LENS.magnify = 1.6F;
      LENS.refraction = -6.0F;
      LENS.specular = 0.9F;
      LENS.lightFollowsMouse = true;
      LENS.tint = Paint.solid(0);
      DROPS.refraction = 8.0F;
      DROPS.dispersion = 0.4F;
      segments.select(2);
   }
}
