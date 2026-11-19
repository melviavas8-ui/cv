package dev.cloudvisuals.module.impl.hud;

import dev.cloudvisuals.hud.Anchor;
import dev.cloudvisuals.hud.HudContext;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.resource.language.I18n;

public final class PingModule extends InfoElement {
   private int latency = -1;

   public PingModule() {
      super("ping", Anchor.TOP_LEFT, 6.0F, 48.0F, true);
   }

   @Override
   public int icon() {
      return 57774;
   }

   @Override
   protected int valueIcon() {
      return 57774;
   }

   @Override
   public void onTick() {
      MinecraftClient mc = MinecraftClient.getInstance();
      this.latency = -1;
      if (mc.getNetworkHandler() != null && mc.player != null && !mc.isIntegratedServerRunning()) {
         PlayerListEntry info = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
         if (info != null) {
            this.latency = info.getLatency();
         }
      }

      if (this.latency >= 0) {
         this.sample(this.latency);
      }
   }

   @Override
   protected boolean hasContent(HudContext hud) {
      return this.latency >= 0;
   }

   @Override
   protected String value(HudContext hud) {
      return this.latency >= 0 ? Integer.toString(this.latency) : (hud.preview ? "42" : "\u2014");
   }

   @Override
   protected String unit(HudContext hud) {
      return I18n.translate("cloudvisuals.unit.ms", new Object[0]);
   }

   @Override
   protected int tint(HudContext hud) {
      int shown = this.latency >= 0 ? this.latency : 42;
      if (shown < 80) {
         return ColorUtil.lerp(Theme.positive, hud.accent(), 0.35F);
      } else {
         return shown < 160 ? Theme.warning : Theme.danger;
      }
   }
}
