package dev.cloudvisuals.module.impl.hud;

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
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.Text;

public final class GlassScoreboardModule extends Module {
   private static final Comparator<ScoreboardEntry> ORDER = Comparator.comparing(ScoreboardEntry::value)
      .reversed()
      .thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER);
   private final BoolSetting numbers = this.add(new BoolSetting("numbers", true));

   public GlassScoreboardModule() {
      super("glass_scoreboard", Category.HUD, false);
   }

   @Override
   public int icon() {
      return 58463;
   }

   public void render(DrawContext g, ScoreboardObjective objective) {
      MinecraftClient mc = MinecraftClient.getInstance();
      TextRenderer font = mc.textRenderer;
      Scoreboard scoreboard = objective.getScoreboard();
      NumberFormat format = objective.getNumberFormatOr(StyledNumberFormat.RED);
      List<GlassScoreboardModule.Line> lines = scoreboard.getScoreboardEntries(objective)
         .stream()
         .filter(entry -> !entry.hidden())
         .sorted(ORDER)
         .limit(15L)
         .map(entry -> {
            Team team = scoreboard.getScoreHolderTeam(entry.owner());
            Text name = Team.decorateName(team, entry.name());
            Text score = entry.formatted(format);
            return new GlassScoreboardModule.Line(name, score, this.numbers.isOn() ? font.getWidth(score) : 0);
         })
         .toList();
      Text title = objective.getDisplayName();
      int titleWidth = font.getWidth(title);
      int colon = font.getWidth(": ");
      int width = titleWidth;

      for (GlassScoreboardModule.Line line : lines) {
         width = Math.max(width, font.getWidth(line.name()) + (line.scoreWidth() > 0 ? colon + line.scoreWidth() : 0));
      }

      int count = lines.size();
      int lineHeight = 9;
      int bottom = g.getScaledWindowHeight() / 2 + count * lineHeight / 3;
      int right = g.getScaledWindowWidth() - 6;
      int left = right - width;
      int top = bottom - count * lineHeight;
      float panelX = left - 6;
      float panelY = top - lineHeight - 8;
      float panelW = width + 12;
      float panelH = bottom - panelY + 5.0F;
      Backdrop.capture();
      Glass glass = Theme.glass().copy();
      glass.tint = Paint.solid(-1710223340);
      Corners corners = Corners.themed(11.0F);
      Surfaces.shadow(g, panelX, panelY + 2.0F, panelW, panelH, corners, new Shadow(1157627904, 9.0F, 0.0F, 0.0F, 3.0F), 1.0F);
      LiquidGlass.draw(g, panelX, panelY, panelW, panelH, corners, glass, 1.0F);
      int accent = Theme.accent();
      Surfaces.fill(
         g,
         panelX + 8.0F,
         top - 3.5F,
         panelW - 16.0F,
         0.8F,
         Corners.NONE,
         ColorUtil.withAlpha(accent, 0),
         ColorUtil.withAlpha(accent, 192),
         ColorUtil.withAlpha(accent, 192),
         ColorUtil.withAlpha(accent, 0),
         1.0F
      );
      g.drawText(font, title, left + width / 2 - titleWidth / 2, top - lineHeight - 3, -1, false);

      for (int i = 0; i < count; i++) {
         GlassScoreboardModule.Line line = lines.get(i);
         int y = bottom - (count - i) * lineHeight;
         g.drawText(font, line.name(), left, y, -1, false);
         if (line.scoreWidth() > 0) {
            g.drawText(font, line.score(), right - line.scoreWidth(), y, -1, false);
         }
      }
   }

   private record Line(Text name, Text score, int scoreWidth) {
      private Line {
      }
   }
}
