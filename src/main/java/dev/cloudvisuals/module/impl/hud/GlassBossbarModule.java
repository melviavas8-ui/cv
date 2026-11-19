package dev.cloudvisuals.module.impl.hud;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.render.Backdrop;
import dev.cloudvisuals.render.LiquidGlass;
import dev.cloudvisuals.render.Surfaces;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.style.Corners;
import dev.cloudvisuals.render.style.Glass;
import dev.cloudvisuals.render.style.Paint;
import dev.cloudvisuals.render.style.Shadow;
import dev.cloudvisuals.util.ColorUtil;
import java.util.Collection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar.Color;
import net.minecraft.text.Text;

public final class GlassBossbarModule extends Module {
   private static final float BAR_W = 182.0F;

   public GlassBossbarModule() {
      super("glass_bossbar", Category.HUD, false);
   }

   @Override
   public int icon() {
      return 58421;
   }

   public void render(DrawContext g, Collection<ClientBossBar> events) {
      if (!events.isEmpty()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         TextRenderer font = mc.textRenderer;
         Backdrop.capture();
         Glass glass = Theme.glass().copy();
         glass.tint = Paint.solid(-1810886636);
         float center = g.getScaledWindowWidth() / 2.0F;
         float y = 6.0F;
         if (Modules.island() != null) {
            y = Math.max(y, Modules.island().reservedTop(g.getScaledWindowWidth()) + 5.0F);
         }

         for (ClientBossBar event : events) {
            float x = center - 91.0F;
            float panelX = x - 6.0F;
            float panelW = 194.0F;
            float panelH = 22.0F;
            Corners corners = Corners.themed(10.0F);
            Surfaces.shadow(g, panelX, y + 2.0F, panelW, panelH, corners, new Shadow(1073741824, 8.0F, 0.0F, 0.0F, 2.0F), 1.0F);
            LiquidGlass.draw(g, panelX, y, panelW, panelH, corners, glass, 1.0F);
            Text name = event.getName();
            g.drawTextWithShadow(font, name, Math.round(center - font.getWidth(name) / 2.0F), Math.round(y + 3.0F), -1);
            float barY = y + 14.0F;
            float barH = 3.5F;
            int color = color(event.getColor());
            Surfaces.fill(g, x, barY, 182.0F, barH, Corners.round(barH / 2.0F), 822083583, 822083583, 822083583, 822083583, 1.0F);
            float filled = 182.0F * Math.clamp(event.getPercent(), 0.0F, 1.0F);
            if (filled > 0.5F) {
               Surfaces.fill(
                  g,
                  x,
                  barY,
                  Math.max(filled, barH),
                  barH,
                  Corners.round(barH / 2.0F),
                  Paint.linear(0.0F, ColorUtil.lerp(color, -16777216, 0.15F), ColorUtil.lerp(color, -1, 0.25F)),
                  1.0F
               );
            }
            int notches = switch (event.getStyle()) {
               case NOTCHED_6 -> 6;
               case NOTCHED_10 -> 10;
               case NOTCHED_12 -> 12;
               case NOTCHED_20 -> 20;
               default -> 0;
            };

            for (int i = 1; i < notches; i++) {
               float notchX = x + 182.0F * i / notches;
               Surfaces.fill(g, notchX - 0.4F, barY - 0.5F, 0.8F, barH + 1.0F, Corners.NONE, -1879048192, -1879048192, -1879048192, -1879048192, 1.0F);
            }

            y += panelH + 4.0F;
            if (y >= g.getScaledWindowHeight() / 3.0F) {
               break;
            }
         }
      }
   }

   private static int color(Color color) {
      return switch (color) {
         case PINK -> -36939;
         case BLUE -> -16087809;
         case RED -> Theme.danger;
         case GREEN -> Theme.positive;
         case YELLOW -> -10742;
         case PURPLE -> -4236558;
         case WHITE -> -1710614;
         default -> throw new MatchException(null, null);
      };
   }
}
