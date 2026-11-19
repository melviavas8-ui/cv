package dev.cloudvisuals.gui.screen;

import dev.cloudvisuals.gui.HudEditorScreen;
import dev.cloudvisuals.gui.menu.MenuScreen;
import dev.cloudvisuals.hud.GameStats;
import dev.cloudvisuals.module.impl.ScreensModule;
import dev.cloudvisuals.render.LiquidGlass;
import dev.cloudvisuals.render.Render2D;
import dev.cloudvisuals.render.Surfaces;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.font.FontWeight;
import dev.cloudvisuals.render.font.Fonts;
import dev.cloudvisuals.render.font.SdfFont;
import dev.cloudvisuals.render.style.Corners;
import dev.cloudvisuals.render.style.Glass;
import dev.cloudvisuals.render.style.Shadow;
import dev.cloudvisuals.server.KnownServers;
import dev.cloudvisuals.util.ColorUtil;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

public final class PauseDesign {
   private static boolean laidOut;
   private static int columnX;
   private static int columnW;
   private static int titleY;
   private static int infoY;

   private PauseDesign() {
   }

   public static boolean active() {
      return ScreensModule.pauseScreen();
   }

   public static void layout(GameMenuScreen screen, int width, int height) {
      laidOut = false;
      if (active() && screen.shouldShowMenu()) {
         List<ClickableWidget> widgets = Screens.getButtons(screen);
         ClickableWidget back = null;
         ClickableWidget advancements = null;
         ClickableWidget stats = null;
         ClickableWidget options = null;
         ClickableWidget lan = null;
         ClickableWidget feedback = null;
         ClickableWidget bugs = null;
         ClickableWidget disconnect = null;
         TextWidget title = null;
         List<ClickableWidget> extra = new ArrayList<>();

         for (ClickableWidget widget : widgets) {
            if (widget instanceof TextWidget text && text.getMessage().equals(screen.getTitle())) {
               title = text;
            } else {
               switch (GlassWidgets.key(widget)) {
                  case "menu.returnToGame":
                     back = widget;
                     break;
                  case "gui.advancements":
                     advancements = widget;
                     break;
                  case "gui.stats":
                     stats = widget;
                     break;
                  case "menu.options":
                     options = widget;
                     break;
                  case "menu.shareToLan":
                  case "menu.playerReporting":
                     lan = widget;
                     break;
                  case "menu.sendFeedback":
                  case "menu.feedback":
                     feedback = widget;
                     break;
                  case "menu.reportBugs":
                  case "menu.server_links":
                     bugs = widget;
                     break;
                  case "menu.returnToMenu":
                  case "menu.disconnect":
                     disconnect = widget;
                     break;
                  default:
                     if (widget.visible) {
                        extra.add(widget);
                     }
               }
            }
         }

         if (back != null && disconnect != null) {
            if (title != null) {
               title.visible = false;
            }

            MinecraftClient mc = MinecraftClient.getInstance();
            ButtonWidget cloudvisuals = ButtonWidget.builder(Text.literal("Cloud Visuals"), button -> mc.setScreen(new MenuScreen(screen))).build();
            ButtonWidget hud = ButtonWidget.builder(
                  Text.translatable("cloudvisuals.screens.hud_editor"), button -> mc.setScreen(new HudEditorScreen(() -> screen))
               )
               .build();
            widgets.add(cloudvisuals);
            widgets.add(hud);
            int buttonH = height >= 280 ? 22 : 20;
            int gap = height >= 280 ? 5 : 4;
            columnW = Math.round(Math.clamp(width * 0.36F, 200.0F, 250.0F));
            columnX = (width - columnW) / 2;
            List<ClickableWidget[]> rows = new ArrayList<>();
            rows.add(new ClickableWidget[]{back});
            rows.add(pair(advancements, stats));
            rows.add(pair(options, lan));
            rows.add(pair(feedback, bugs));
            rows.add(pair(cloudvisuals, hud));

            for (int i = 0; i < extra.size(); i += 2) {
               rows.add(pair(extra.get(i), i + 1 < extra.size() ? extra.get(i + 1) : null));
            }

            rows.removeIf(row -> row.length == 0);
            int header = 16 + (ScreensModule.pauseInfo() ? 30 : 6);
            int total = header + rows.size() * (buttonH + gap) + 6 + buttonH;
            int top = Math.max(6, (height - total) / 2);
            titleY = top;
            infoY = top + 20;
            int y = top + header;

            for (ClickableWidget[] row : rows) {
               if (row.length == 1) {
                  row[0].setDimensionsAndPosition(columnW, buttonH, columnX, y);
               } else {
                  int half = (columnW - gap) / 2;
                  row[0].setDimensionsAndPosition(half, buttonH, columnX, y);
                  row[1].setDimensionsAndPosition(columnW - half - gap, buttonH, columnX + half + gap, y);
               }

               y += buttonH + gap;
            }

            disconnect.setDimensionsAndPosition(columnW, buttonH, columnX, y + 6);
            GlassWidgets.style(back).icon(57660).tint(ColorUtil.withAlpha(Theme.accent(), 92), false);
            icon(advancements, 58227);
            icon(stats, 58019);
            icon(options, 57684);
            if (lan != null) {
               icon(lan, "menu.shareToLan".equals(GlassWidgets.key(lan)) ? 57637 : 57764);
            }

            if (feedback != null) {
               icon(feedback, 57623);
            }

            if (bugs != null) {
               icon(bugs, "menu.server_links".equals(GlassWidgets.key(bugs)) ? 57602 : 57868);
            }

            icon(cloudvisuals, 57525);
            icon(hud, 57793);

            for (ClickableWidget widget : extra) {
               icon(widget, "modmenu.title".equals(GlassWidgets.key(widget)) ? 57641 : 0);
            }

            GlassWidgets.style(disconnect).icon(57614).tint(ColorUtil.withAlpha(Theme.danger, 144), true);
            laidOut = true;
         }
      }
   }

   private static ClickableWidget[] pair(ClickableWidget a, ClickableWidget b) {
      if (a == null && b == null) {
         return new ClickableWidget[0];
      } else {
         return a != null && b != null ? new ClickableWidget[]{a, b} : new ClickableWidget[]{a == null ? b : a};
      }
   }

   private static void icon(ClickableWidget widget, int icon) {
      if (widget != null && icon != 0) {
         GlassWidgets.style(widget).icon(icon);
      }
   }

   public static void renderHeader(DrawContext g, GameMenuScreen screen) {
      if (laidOut && active() && screen.shouldShowMenu()) {
         try {
            SdfFont display = Fonts.get(FontWeight.DISPLAY_BOLD);
            if (display == null) {
               return;
            }

            String title = screen.getTitle().getString();
            float size = 12.0F;
            int cx = screen.width / 2;
            if (display.canDisplay(title)) {
               display.draw(g, title, cx - display.width(title, size, 0.4F) / 2.0F, titleY, size, 0.4F, -1);
            } else {
               int var10004 = titleY + 2;
               g.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, title, cx, var10004, -1);
            }

            if (ScreensModule.pauseInfo()) {
               info(g, cx);
            }
         } catch (RuntimeException e) {
            ScreensModule.crashed(e);
         }
      }
   }

   private static void info(DrawContext g, int centerX) {
      SdfFont font = Fonts.get(FontWeight.MEDIUM);
      SdfFont icons = Fonts.icons();
      if (font != null && icons != null) {
         MinecraftClient mc = MinecraftClient.getInstance();
         List<int[]> segmentIcons = new ArrayList<>();
         List<String> texts = new ArrayList<>();
         if (mc.isIntegratedServerRunning() && mc.getServer() != null) {
            segmentIcons.add(new int[]{57567});
            texts.add(mc.getServer().getSaveProperties().getLevelName());
         } else {
            ServerInfo data = mc.getCurrentServerEntry();
            String host = KnownServers.currentHost();
            String name = KnownServers.whitelisted(host)
               .map(KnownServers.Server::name)
               .orElse(data != null && (mc.options.hideServerAddress || host == null) ? data.name : host);
            segmentIcons.add(new int[]{57683});
            texts.add(name == null ? "?" : name);
            PlayerListEntry self = mc.getNetworkHandler() != null && mc.player != null ? mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()) : null;
            if (self != null) {
               segmentIcons.add(new int[]{57774});
               texts.add(I18n.translate("cloudvisuals.screens.ping", new Object[]{self.getLatency()}));
            }

            if (mc.getNetworkHandler() != null) {
               segmentIcons.add(new int[]{57764});
               texts.add(String.valueOf(mc.getNetworkHandler().getPlayerList().size()));
            }
         }

         long seconds = GameStats.sessionMillis() / 1000L;
         segmentIcons.add(new int[]{57479});
         texts.add(
            seconds >= 3600L
               ? String.format("%d:%02d:%02d", seconds / 3600L, seconds / 60L % 60L, seconds % 60L)
               : String.format("%d:%02d", seconds / 60L, seconds % 60L)
         );
         float size = 7.8F;
         float iconSize = 9.0F;
         float inner = 4.0F;
         float between = 11.0F;
         float pad = 9.0F;
         float h = 20.0F;
         float total = pad * 2.0F - between;
         float[] widths = new float[texts.size()];

         for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            if (!font.canDisplay(text)) {
               text = text.replaceAll("[^\\x20-\\x7E\\u0400-\\u045F]", "?");
            }

            texts.set(i, text);
            widths[i] = font.width(text, size);
            total += iconSize + inner + widths[i] + between;
         }

         float x = centerX - total / 2.0F;
         float y = infoY;
         Glass glass = Theme.glass();
         Surfaces.shadow(g, x, y + 2.0F, total, h, Corners.round(h / 2.0F), new Shadow(1073741824, 10.0F, 0.0F, 0.0F, 3.0F), 1.0F);
         LiquidGlass.draw(g, x, y, total, h, Corners.round(h / 2.0F), glass, 1.0F);
         float pen = x + pad;

         for (int i = 0; i < texts.size(); i++) {
            if (i > 0) {
               Render2D.circle(g, pen - between / 2.0F, y + h / 2.0F, 1.1F, ColorUtil.withAlpha(glass.label, 80));
            }

            icons.drawIcon(g, segmentIcons.get(i)[0], pen + iconSize / 2.0F, y + h / 2.0F, iconSize, ColorUtil.withAlpha(Theme.accent(), 255));
            pen += iconSize + inner;
            font.draw(g, texts.get(i), pen, y + h / 2.0F - font.ascent(size) + font.capHeight(size) / 2.0F, size, ColorUtil.withAlpha(glass.label, 230));
            pen += widths[i] + between;
         }
      }
   }
}
