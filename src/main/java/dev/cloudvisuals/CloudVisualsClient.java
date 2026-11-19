package dev.cloudvisuals;

import dev.cloudvisuals.config.BuildLinks;
import dev.cloudvisuals.config.ConfigManager;
import dev.cloudvisuals.config.CrashGuard;
import dev.cloudvisuals.config.UpdateChecker;
import dev.cloudvisuals.gui.CloudVisualsMenuButton;
import dev.cloudvisuals.gui.HudEditorScreen;
import dev.cloudvisuals.gui.PersonalTest;
import dev.cloudvisuals.gui.PromoRecorder;
import dev.cloudvisuals.gui.ScreensTest;
import dev.cloudvisuals.gui.WorldTest;
import dev.cloudvisuals.gui.menu.MenuScreen;
import dev.cloudvisuals.gui.screen.GlassSearchField;
import dev.cloudvisuals.gui.screen.PauseDesign;
import dev.cloudvisuals.gui.screen.PlayerPreview;
import dev.cloudvisuals.gui.screen.TitleDesign;
import dev.cloudvisuals.hud.GameStats;
import dev.cloudvisuals.hud.TargetTracker;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.visual.BlockOutlineModule;
import dev.cloudvisuals.notify.Notifications;
import dev.cloudvisuals.render.GlowEngine;
import dev.cloudvisuals.render.Shaders;
import dev.cloudvisuals.render.font.Fonts;
import dev.cloudvisuals.render.world.CosmeticsLayer;
import dev.cloudvisuals.render.world.WorldCosmetics;
import dev.cloudvisuals.server.ServerProfiles;
import dev.cloudvisuals.server.HandshakePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import java.util.Locale;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents.AfterEntities;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents.BeforeBlockOutline;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents.EndMain;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents.StartMain;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterInit;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterRender;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudVisualsClient implements ClientModInitializer {
   public static final String MOD_ID = "cloudvisuals";
   public static final Logger LOGGER = LoggerFactory.getLogger("CloudVisuals");
   private static boolean hudLayerInvoked;
   private static boolean hudLayerRendered;
   private static boolean hudLayerFailed;
   private static final boolean DEMO = Boolean.getBoolean("cloudvisuals.demo");
   private static final String UI_TEST = readUiTest();
   private static boolean demoOpened;
   private static boolean welcomed;

   public CloudVisualsClient() {
   }

   public static String uiTest() {
      return UI_TEST;
   }

   private static String readUiTest() {
      String value = System.getProperty("cloudvisuals.uitest", "false").trim();
      return !value.isEmpty() && !value.equalsIgnoreCase("false") ? value.toLowerCase(Locale.ROOT) : null;
   }

   public static String version() {
      String full = fullVersion();
      int plus = full.indexOf(43);
      return plus > 0 ? full.substring(0, plus) : full;
   }

   public static String fullVersion() {
      return FabricLoader.getInstance().getModContainer("cloudvisuals").map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("dev");
   }

   public void onInitializeClient() {
      Shaders.register();
      PlayerPreview.register();
      Fonts.preload();
      Modules.init();
      ClientLifecycleEvents.CLIENT_STARTED.register((ClientStarted)client -> {
         ConfigManager.load();
         Modules.onConfigLoaded();
         ServerProfiles.restoreAfterCrash();
         CrashGuard.onStart();
         UpdateChecker.start(Modules.notifications() == null || Modules.notifications().checkUpdates());
      });
      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)client -> {
         ConfigManager.save();
         CrashGuard.onStop();
      });
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         Modules.onTick();
         ConfigManager.tick();
         if (UI_TEST != null) {
            WorldTest.tick(client);
            PromoRecorder.tick(client);
            ScreensTest.tick(client);
            PersonalTest.tick(client);
         }
      });
      HudElementRegistry.addLast(Identifier.of("cloudvisuals", "hud"), (graphics, deltaTracker) -> {
         try {
            if (MinecraftClient.getInstance().options.hudHidden) {
               return;
            }

            if (!hudLayerInvoked) {
               hudLayerInvoked = true;
               LOGGER.info("[Cloud Visuals] HUD layer invoked (first frame)");
            }

            Modules.onHud(graphics, deltaTracker);
            if (!hudLayerRendered) {
               hudLayerRendered = true;
               LOGGER.info("[Cloud Visuals] HUD render OK (Modules.onHud completed)");
            }

            if (MinecraftClient.getInstance().currentScreen == null) {
               Notifications.render(graphics);
            }
         } catch (Throwable t) {
            if (!hudLayerFailed) {
               hudLayerFailed = true;
               LOGGER.error("[Cloud Visuals] HUD layer crashed", t);
            }
         }
      });
      ScreenEvents.AFTER_INIT.register((AfterInit)(client, screen, width, height) -> {
         if (UI_TEST != null) {
            ScreenEvents.afterRender(screen).register((AfterRender)(current, graphics, mouseX, mouseY, delta) -> {
               if (WorldTest.tooltipDemo) {
                  graphics.drawItemTooltip(client.textRenderer, new ItemStack(Items.DIAMOND_SWORD), width / 2 + 40, height / 2 - 30);
                  graphics.drawDeferredElements();
               }
            });
         }
      });
      ScreenEvents.AFTER_INIT
         .register(
            (AfterInit)(client, screen, width, height) -> {
               ScreenEvents.afterRender(screen).register((AfterRender)(current, graphics, mouseX, mouseY, delta) -> Notifications.render(graphics));
               if (Modules.containerSearch() != null) {
                  GlassSearchField search = Modules.containerSearch().attach(screen);
                  if (search != null) {
                     Screens.getButtons(screen).add(search);
                  }
               }

               if (screen instanceof TitleScreen title) {
                  if (dev.cloudvisuals.module.impl.ScreensModule.titleScreen()) {
                     Screens.getButtons(screen).add(new CloudVisualsMenuButton(screen, width - 74, 8, 66, 20));
                     TitleDesign.layout(title, width, height);
                  }
               } else if (screen instanceof GameMenuScreen pause && pause.shouldShowMenu()) {
                  if (PauseDesign.active()) {
                     PauseDesign.layout(pause, width, height);
                  } else {
                     Screens.getButtons(screen).add(new CloudVisualsMenuButton(screen, width - 74, 8, 66, 20));
                  }
               }

               if (!welcomed && screen instanceof TitleScreen) {
                  welcomed = true;
                  Notifications.show(
                     Notifications.Kind.INFO,
                     I18n.translate("cloudvisuals.notify.welcome.title", new Object[]{version()}),
                     I18n.translate("cloudvisuals.notify.welcome.subtitle", new Object[0])
                  );
               }

               if (DEMO
                  && !demoOpened
                  && screen instanceof TitleScreen
                  && !WorldTest.handles(UI_TEST)
                  && !PromoRecorder.handles(UI_TEST)
                  && !"screens".equals(UI_TEST)
                  && !"personal".equals(UI_TEST)) {
                  demoOpened = true;
                  if ("hud".equals(UI_TEST)) {
                     client.send(() -> client.setScreen(new HudEditorScreen(() -> screen)));
                  } else {
                     client.send(() -> client.setScreen(new MenuScreen(screen)));
                  }
               }
            }
         );
      // Register C2S handshake payload
      PayloadTypeRegistry.playC2S().register(HandshakePayload.ID, HandshakePayload.CODEC);

      ClientPlayConnectionEvents.JOIN.register((Join)(handler, sender, client) -> client.execute(() -> {
         Modules.serverWarning().onJoin();
         if (Modules.eventSounds() != null) {
            Modules.eventSounds().onJoin();
         }

         ServerProfiles.onJoin();
         GameStats.sessionStarted();

         // Tell the server that CloudVisuals is installed
         try {
            ClientPlayNetworking.send(new HandshakePayload());
         } catch (Exception e) {
            LOGGER.warn("[Cloud Visuals] failed to send handshake", e);
         }
      }));
      ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> {
         GameStats.sessionEnded();
         client.execute(ServerProfiles::onLeave);
      });
      TargetTracker.register();
      WorldRenderEvents.START_MAIN.register((StartMain)context -> GlowEngine.beginWorld());
      WorldRenderEvents.AFTER_ENTITIES.register((AfterEntities)context -> GlowEngine.refreshDepth());
      WorldRenderEvents.AFTER_ENTITIES.register(WorldCosmetics::render);
      LivingEntityFeatureRendererRegistrationCallback.EVENT
         .register((LivingEntityFeatureRendererRegistrationCallback)(entityType, renderer, helper, context) -> {
            if (renderer instanceof PlayerEntityRenderer<?> player) {
               helper.register(new CosmeticsLayer(player));
            }
         });
      WorldRenderEvents.END_MAIN.register((EndMain)context -> {
         if (Modules.trajectories() != null) {
            Modules.guard(Modules.trajectories(), () -> Modules.trajectories().render(context));
         }
      });
      WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((BeforeBlockOutline)(context, outline) -> {
         BlockOutlineModule module = Modules.blockOutline();
         if (module != null && module.isEnabled() && outline != null) {
            BlockPos pos = outline.pos();
            Vec3d camera = context.worldState().cameraRenderState.pos;

            try {
               module.render(context.matrices(), outline.shape(), pos, camera.x, camera.y, camera.z);
               return false;
            } catch (RuntimeException e) {
               Modules.fail(module, e);
               return true;
            }
         } else {
            return true;
         }
      });
      WorldRenderEvents.END_MAIN.register((EndMain)context -> GlowEngine.endWorld());
      LOGGER.info("[Cloud Visuals] initialized, version {}, {} modules", version(), Modules.all().size());

      for (String link : BuildLinks.all()) {
         LOGGER.info("[Cloud Visuals] official: {}", link);
      }
   }
}
