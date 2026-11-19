package dev.cloudvisuals.module.impl.visual;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.render.Backdrop;
import dev.cloudvisuals.render.LiquidGlass;
import dev.cloudvisuals.render.Surfaces;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.style.Corners;
import dev.cloudvisuals.render.style.Glass;
import dev.cloudvisuals.render.style.Paint;
import dev.cloudvisuals.render.style.Shadow;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;

public final class GlassTooltipsModule extends Module {
   private final BoolSetting accentLine = this.add(new BoolSetting("accent_line", true));

   public GlassTooltipsModule() {
      super("glass_tooltips", Category.VISUALS, false);
   }

   @Override
   public int icon() {
      return 58741;
   }

   public void renderBackground(DrawContext g, int x, int y, int w, int h, int z) {
      Backdrop.capture();
      g.getMatrices().pushMatrix();
      g.getMatrices().translate(0.0F, 0.0F);
      float px = x - 4;
      float py = y - 4;
      float pw = w + 8;
      float ph = h + 8;
      Corners corners = Corners.themed(7.0F);
      Glass glass = Theme.glass().copy();
      glass.tint = Paint.solid(-938471402);
      glass.refraction = 4.0F;
      glass.bevel = 5.0F;
      Surfaces.shadow(g, px, py + 2.0F, pw, ph, corners, new Shadow(1610612736, 8.0F, 0.0F, 0.0F, 3.0F), 1.0F);
      LiquidGlass.draw(g, px, py, pw, ph, corners, glass, 1.0F);
      if (this.accentLine.isOn()) {
         int accent = Theme.accent();
         Surfaces.border(
            g, px, py, pw, ph, corners, 0.8F, Paint.linear(90.0F, ColorUtil.withAlpha(accent, 192), ColorUtil.withAlpha(Theme.accent2(), 64)), 1.0F
         );
      }

      g.getMatrices().popMatrix();
   }
}
