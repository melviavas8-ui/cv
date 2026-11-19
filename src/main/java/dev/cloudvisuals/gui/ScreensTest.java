package dev.cloudvisuals.gui;

import com.google.gson.JsonPrimitive;
import dev.cloudvisuals.CloudVisualsClient;
import dev.cloudvisuals.gui.screen.ServerListDesign;
import dev.cloudvisuals.mixin.JoinMultiplayerScreenAccessor;
import dev.cloudvisuals.mixin.MouseHandlerAccessor;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.module.Modules;
import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.client.network.ServerInfo.Status;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.server.ServerMetadata.Players;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;

public final class ScreensTest {
   private static final String[][] SERVERS = new String[][]{
      {
            "FunTime",
            "mc.funtime.su",
            "\u00a76FunTime \u00a77\u2014 \u00a7f\u0433\u0440\u0438\u0444\u0435\u0440\u0441\u0442\u0432\u043e \u0438 \u0430\u043d\u0430\u0440\u0445\u0438\u044f",
            "18432",
            "25000",
            "38"
      },
      {
            "CloudVisuals Test",
            "127.0.0.1:25599",
            "\u00a7b\u041b\u043e\u043a\u0430\u043b\u044c\u043d\u044b\u0439 \u0442\u0435\u0441\u0442\u043e\u0432\u044b\u0439 \u0441\u0435\u0440\u0432\u0435\u0440",
            "3",
            "20",
            "4"
      },
      {"HolyWorld", "mc.holyworld.me", "\u00a7dHolyWorld \u00a77| \u00a7f\u0432\u044b\u0436\u0438\u0432\u0430\u043d\u0438\u0435", "9120", "15000", "142"},
      {"Friends", "192.168.1.40", "\u00a7a\u0421\u0435\u0440\u0432\u0435\u0440 \u0441 \u0434\u0440\u0443\u0437\u044c\u044f\u043c\u0438", "2", "10", "260"}
   };
   private static long stepAt = -1L;
   private static int step;
   private static boolean done;

   private ScreensTest() {
   }

   public static void tick(MinecraftClient mc) {
      if ("screens".equals(CloudVisualsClient.uiTest()) && !done && (mc.getOverlay() == null || step == 3 || step == 4)) {
         long now = System.currentTimeMillis();
         if (stepAt < 0L) {
            if (!(mc.currentScreen instanceof TitleScreen)) {
               return;
            }

            stepAt = now + 5000L;
         }

         if (now >= stepAt) {
            long next = run(mc, step);
            if (next >= 0L) {
               CloudVisualsClient.LOGGER.info("[Cloud Visuals][screenstest] step {} done", step);
               step++;
               stepAt = now + next;
            }
         }
      }
   }

   private static long run(MinecraftClient mc, int index) {
      switch (index) {
         case 0:
            shot(mc, "title");
            break;
         case 1:
            if (mc.currentScreen instanceof TitleScreen screen) {
               hover(mc, find(screen, "menu.multiplayer"));
               return 700L;
            }

            return -1L;
         case 2:
            shot(mc, "title_hover");
            mc.reloadResources();
            return 900L;
         case 3:
            shot(mc, "loading_a");
            return 1300L;
         case 4:
            shot(mc, "loading_b");
            return 1000L;
         case 5:
            if (mc.getOverlay() == null && mc.currentScreen instanceof TitleScreen screen) {
               prepareServers(mc);
               mc.setScreen(new MultiplayerScreen(screen));
               fakeStatus(mc);
               mouse(mc, 20.0, 20.0);
               return 1500L;
            }

            return -1L;
         case 6:
            shot(mc, "servers");
            break;
         case 7:
            mouse(mc, 320.0, 136.0);
            return 800L;
         case 8:
            shot(mc, "servers_hover");
            if (mc.currentScreen instanceof MultiplayerScreen screen) {
               ServerInfo third = screen.getServerList().get(2);
               ServerListDesign.testToggleFavorite(screen, third);
            }

            return 900L;
         case 9:
            shot(mc, "servers_favorite");
            if (mc.currentScreen instanceof MultiplayerScreen screen) {
               mc.setScreen(
                  new AddServerScreen(
                     screen,
                     Text.translatable("manageServer.add.title"),
                     accepted -> mc.setScreen(screen),
                     new ServerInfo("Cloud Visuals", "", ServerType.OTHER)
                  )
               );
            }

            return 1000L;
         case 10:
            shot(mc, "edit_server");
            if (mc.currentScreen instanceof AddServerScreen edit) {
               mc.setScreen(new MultiplayerWarningScreen(edit));
            }

            return 1000L;
         case 11:
            shot(mc, "safety");
            if (mc.currentScreen instanceof MultiplayerWarningScreen) {
               mc.setScreen(new MultiplayerScreen(new TitleScreen()));
               fakeStatus(mc);
            }

            return 1200L;
         case 12:
            if (mc.currentScreen instanceof MultiplayerScreen screen) {
               ConnectScreen.connect(
                  screen,
                  mc,
                  ServerAddress.parse("10.255.255.1:25565"),
                  new ServerInfo("CloudVisuals Test", "10.255.255.1:25565", ServerType.OTHER),
                  false,
                  null
               );
            }

            mouse(mc, 20.0, 20.0);
            return 1500L;
         case 13:
            shot(mc, "connecting");
            if (mc.currentScreen instanceof ConnectScreen screen) {
               press(screen, "gui.cancel");
            }

            return 1200L;
         case 14:
            if (mc.currentScreen instanceof MultiplayerScreen screen) {
               ConnectScreen.connect(
                  screen, mc, ServerAddress.parse("127.0.0.1:1"), new ServerInfo("CloudVisuals Test", "127.0.0.1:1", ServerType.OTHER), false, null
               );
               return 2500L;
            }

            return -1L;
         case 15:
            if (!(mc.currentScreen instanceof DisconnectedScreen)) {
               return -1L;
            }

            shot(mc, "disconnected");
            mc.setScreen(new TitleScreen());
            return 1500L;
         case 16:
            WorldTest.enterWorldForTest(mc);
            return 100L;
         case 17:
            if (!(mc.currentScreen instanceof LevelLoadingScreen)) {
               return mc.world != null ? 0L : -1L;
            }

            return 350L;
         case 18:
            if (mc.currentScreen instanceof LevelLoadingScreen) {
               shot(mc, "world_loading");
            }

            return 0L;
         case 19:
            if (mc.world != null && mc.player != null && mc.currentScreen == null) {
               mc.options.hudHidden = false;
               return 3500L;
            }

            return -1L;
         case 20:
            mc.openGameMenu(false);
            mouse(mc, 20.0, 20.0);
            return 1200L;
         case 21:
            shot(mc, "pause");
            if (mc.currentScreen instanceof GameMenuScreen screen) {
               hover(mc, find(screen, "menu.returnToGame"));
            }

            return 700L;
         case 22:
            shot(mc, "pause_hover");
            if (mc.currentScreen instanceof GameMenuScreen screen) {
               mc.setScreen(new OptionsScreen(screen, mc.options));
            }

            mouse(mc, 20.0, 20.0);
            return 1200L;
         case 23:
            shot(mc, "options");
            Module performance = Modules.byId("performance");
            if (performance != null) {
               performance.settings().stream().filter(setting -> setting.id().equals("lite")).forEach(setting -> setting.fromJson(new JsonPrimitive(true)));
            }

            return 1200L;
         case 24:
            shot(mc, "options_lite");
            CloudVisualsClient.LOGGER.info("[Cloud Visuals][screenstest] finished");
            done = true;
      }

      return 900L;
   }

   private static void shot(MinecraftClient mc, String name) {
      ScreenshotRecorder.saveScreenshot(mc.runDirectory, "cloudvisuals_" + name + ".png", mc.getFramebuffer(), 1, message -> {});
      CloudVisualsClient.LOGGER.info("[Cloud Visuals][screenstest] shot {}", name);
   }

   private static ClickableWidget find(Screen screen, String key) {
      for (ClickableWidget widget : Screens.getButtons(screen)) {
         if (widget.getMessage().getContent() instanceof TranslatableTextContent contents && contents.getKey().equals(key)) {
            return widget;
         }
      }

      return null;
   }

   private static void press(Screen screen, String key) {
      ClickableWidget widget = find(screen, key);
      if (widget != null) {
         widget.onClick(new Click(widget.getX() + 1, widget.getY() + 1, new MouseInput(0, 0)), false);
      }
   }

   private static void hover(MinecraftClient mc, ClickableWidget widget) {
      if (widget != null) {
         mouse(mc, widget.getX() + widget.getWidth() / 2.0, widget.getY() + widget.getHeight() / 2.0);
      }
   }

   private static void mouse(MinecraftClient mc, double guiX, double guiY) {
      double scale = mc.getWindow().getScaleFactor();
      MouseHandlerAccessor accessor = (MouseHandlerAccessor)mc.mouse;
      accessor.cloudvisuals$setX(guiX * scale);
      accessor.cloudvisuals$setY(guiY * scale);
   }

   private static void prepareServers(MinecraftClient mc) {
      ServerList list = new ServerList(mc);
      list.loadFile();

      while (list.size() > 0) {
         list.remove(list.get(0));
      }

      for (String[] server : SERVERS) {
         list.add(new ServerInfo(server[0], server[1], ServerType.OTHER), false);
      }

      list.saveFile();
   }

   private static void fakeStatus(MinecraftClient mc) {
      if (mc.currentScreen instanceof MultiplayerScreen screen) {
         ServerList var11 = screen.getServerList();

         for (int i = 0; i < var11.size(); i++) {
            ServerInfo data = var11.get(i);

            for (String[] server : SERVERS) {
               if (server[1].equals(data.address)) {
                  data.setStatus(Status.SUCCESSFUL);
                  data.label = Text.literal(server[2]);
                  int online = Integer.parseInt(server[3]);
                  int max = Integer.parseInt(server[4]);
                  data.players = new Players(max, online, List.of());
                  data.playerCountLabel = Text.literal(online + "")
                     .formatted(Formatting.GRAY)
                     .append(Text.literal("/").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal(max + "").formatted(Formatting.GRAY));
                  data.ping = Long.parseLong(server[5]);
               }
            }
         }

         ((JoinMultiplayerScreenAccessor)screen).cloudvisuals$serverSelectionList().setServers(var11);
      }
   }
}
