package dev.cloudvisuals.module.impl.hud;

import dev.cloudvisuals.hud.Anchor;
import dev.cloudvisuals.hud.HudContext;
import dev.cloudvisuals.hud.HudElement;
import dev.cloudvisuals.hud.Size;
import dev.cloudvisuals.render.Surfaces;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.style.Corners;
import dev.cloudvisuals.render.style.Paint;
import dev.cloudvisuals.server.KnownServers;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.ChoiceSetting;
import dev.cloudvisuals.setting.TextSetting;
import dev.cloudvisuals.util.ColorUtil;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.resource.language.I18n;

public final class WatermarkModule extends HudElement {
   private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
   private static final float TEXT = 7.5F;
   private static final float PAD = 8.0F;
   private static final float DOT = 5.0F;
   private final ChoiceSetting<WatermarkModule.Style> style = this.add(new ChoiceSetting<>("style", WatermarkModule.Style.CAPSULE));
   private final TextSetting text = this.add(new TextSetting("text", "Cloud Visuals", 24));
   private final BoolSetting fps = this.add(new BoolSetting("fps", true));
   private final BoolSetting ping = this.add(new BoolSetting("ping", true));
   private final BoolSetting time = this.add(new BoolSetting("time", true));
   private final BoolSetting server = this.add(new BoolSetting("server", true));
   private String info;

   public WatermarkModule() {
      super("watermark", true, Anchor.TOP_LEFT, 6.0F, 6.0F);
   }

   @Override
   public int icon() {
      return 57921;
   }

   @Override
   protected float defaultRoundness() {
      return 1.0F;
   }

   @Override
   public void onTick() {
      this.info = String.join(" \u00b7 ", this.infoParts(MinecraftClient.getInstance()));
   }

   @Override
   protected Size measure(HudContext hud) {
      if (this.info == null) {
         this.onTick();
      }

      String title = this.title();

      return switch ((WatermarkModule.Style)this.style.get()) {
         case CAPSULE -> {
            float infoWidth = this.info.isEmpty() ? 0.0F : 6.0F + hud.regular.width(this.info, 7.5F);
            yield new Size(18.0F + hud.semibold.width(title, 7.5F) + infoWidth + 8.0F, 18.0F);
         }
         case LOGO -> {
            float textWidth = Math.max(hud.displayBold.width(title.toUpperCase(), 9.0F, 0.6F), hud.regular.width(this.info, 6.2F));
            yield new Size(34.0F + textWidth + 10.0F, 32.0F);
         }
         case MINIMAL -> {
            float infoWidth = this.info.isEmpty() ? 0.0F : 7.0F + hud.regular.width(this.info, 6.8F);
            yield new Size(8.0F + hud.displayBold.width(title, 10.5F) + infoWidth + 8.0F, 20.0F);
         }
      };
   }

   @Override
   protected void render(HudContext hud, float w, float h) {
      hud.clip(0.0F, 0.0F, w, h);
      switch ((WatermarkModule.Style)this.style.get()) {
         case CAPSULE:
            this.capsule(hud, h);
            break;
         case LOGO:
            this.logo(hud, h);
            break;
         case MINIMAL:
            this.minimal(hud, h);
      }

      hud.unclip();
   }

   private void capsule(HudContext hud, float h) {
      float cursor = 8.0F;
      hud.fill(cursor, (h - 5.0F) / 2.0F, 5.0F, 5.0F, 2.5F, hud.accent());
      cursor += 10.0F;
      float textY = HudContext.centered(0.0F, h, 7.5F);
      cursor += hud.text(hud.semibold, this.title(), cursor, textY, 7.5F, hud.label);
      if (!this.info.isEmpty()) {
         hud.text(hud.regular, this.info, cursor + 6.0F, textY, 7.5F, hud.secondary);
      }
   }

   private void logo(HudContext hud, float h) {
      float tile = 22.0F;
      float tileX = 5.0F;
      float tileY = (h - tile) / 2.0F;
      int accent = hud.accent();
      int accent2 = Theme.accent2();
      Surfaces.fill(hud.g, tileX, tileY, tile, tile, Corners.themed(8.0F), Paint.linear(45.0F + Theme.time() * 20.0F % 360.0F, accent, accent2), hud.alpha);
      Surfaces.fill(hud.g, tileX, tileY, tile, tile, Corners.themed(8.0F), Paint.linear(90.0F, 1442840575, 16777215), hud.alpha);
      hud.icon(57525, tileX + tile / 2.0F, tileY + tile / 2.0F, 12.0F, -1);
      float textX = tileX + tile + 7.0F;
      String title = this.title().toUpperCase();
      hud.displayBold.draw(hud.g, title, textX, 6.5F, 9.0F, 0.6F, ColorUtil.fade(hud.label, hud.alpha));
      if (!this.info.isEmpty()) {
         hud.text(hud.regular, this.info, textX, 18.5F, 6.2F, hud.secondary);
      }
   }

   private void minimal(HudContext hud, float h) {
      String title = this.title();
      float size = 10.5F;
      float y = HudContext.centered(0.0F, h, size);
      int accent = hud.accent();
      int accent2 = Theme.accent2();
      if (this.background() == HudElement.Background.NONE) {
         hud.displayBold.draw(hud.g, title, 8.6F, y + 0.6F, size, ColorUtil.withAlpha(0, Math.round(110.0F * hud.alpha)));
      }

      hud.displayBold.drawGradient(hud.g, title, 8.0F, y, size, ColorUtil.fade(accent, hud.alpha), ColorUtil.fade(accent2, hud.alpha));
      if (!this.info.isEmpty()) {
         float x = 8.0F + hud.displayBold.width(title, size) + 7.0F;
         hud.text(hud.regular, this.info, x, HudContext.centered(0.0F, h, 6.8F) + 0.6F, 6.8F, hud.secondary);
      }
   }

   private String title() {
      return this.text.get().isBlank() ? "Cloud Visuals" : this.text.get();
   }

   private List<String> infoParts(MinecraftClient mc) {
      List<String> parts = new ArrayList<>();
      if (this.fps.isOn()) {
         parts.add(mc.getCurrentFps() + " fps");
      }

      if (this.ping.isOn() && mc.getNetworkHandler() != null && mc.player != null && !mc.isIntegratedServerRunning()) {
         PlayerListEntry player = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
         if (player != null) {
            parts.add(player.getLatency() + " " + I18n.translate("cloudvisuals.unit.ms", new Object[0]));
         }
      }

      if (this.time.isOn()) {
         parts.add(LocalTime.now().format(TIME));
      }

      if (this.server.isOn()) {
         parts.add(KnownServers.currentName());
      }

      return parts;
   }

   public enum Style {
      CAPSULE,
      LOGO,
      MINIMAL;

      Style() {
      }
   }
}
