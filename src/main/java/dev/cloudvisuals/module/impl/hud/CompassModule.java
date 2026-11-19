package dev.cloudvisuals.module.impl.hud;

import dev.cloudvisuals.hud.Anchor;
import dev.cloudvisuals.hud.HudContext;
import dev.cloudvisuals.hud.HudElement;
import dev.cloudvisuals.hud.Size;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.NumberSetting;
import dev.cloudvisuals.util.ColorUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.MathHelper;

public final class CompassModule extends HudElement {
   private static final float SPAN = 130.0F;
   private final NumberSetting width = this.add(new NumberSetting("width", 190.0, 110.0, 320.0, 10.0, "cloudvisuals.unit.pixels"));
   private final BoolSetting degrees = this.add(new BoolSetting("degrees", true));

   public CompassModule() {
      super("compass", false, Anchor.TOP, 0.0F, 34.0F);
   }

   @Override
   public int icon() {
      return 57499;
   }

   @Override
   protected float defaultRoundness() {
      return 1.0F;
   }

   @Override
   protected Size measure(HudContext hud) {
      return new Size(this.width.floatValue(), this.degrees.isOn() ? 25.0F : 18.0F);
   }

   @Override
   protected void render(HudContext hud, float w, float h) {
      ClientPlayerEntity player = hud.mc.player;
      float yaw = player != null ? MathHelper.lerpAngleDegrees(hud.partialTick, player.lastYaw, player.getYaw()) : 200.0F;
      float heading = MathHelper.floorMod(yaw + 180.0F, 360.0F);
      float center = w / 2.0F;
      float pixelsPerDegree = (w - 16.0F) / 130.0F;
      float band = 18.0F;
      hud.clip(4.0F, 0.0F, w - 8.0F, band);
      int base = hud.label;
      int first = (int)Math.floor((heading - 65.0F) / 15.0F) * 15;

      for (int angle = first; angle <= heading + 65.0F + 15.0F; angle += 15) {
         float x = center + (angle - heading) * pixelsPerDegree;
         float edge = Math.abs(x - center) / (w / 2.0F - 6.0F);
         float fade = Math.clamp(1.0F - edge * edge, 0.0F, 1.0F);
         if (!(fade <= 0.01F)) {
            int normalized = Math.floorMod(angle, 360);
            if (normalized % 90 == 0) {
               String letter = I18n.translate("cloudvisuals.hud.compass." + normalized, new Object[0]);
               float size = 7.2F;
               int color = normalized == 0 ? hud.accent() : base;
               float textW = hud.semibold.width(letter, size);
               hud.text(hud.semibold, letter, x - textW / 2.0F, HudContext.centered(0.0F, band, size), size, ColorUtil.fade(color, fade));
            } else if (normalized % 45 == 0) {
               String letters = I18n.translate("cloudvisuals.hud.compass." + normalized, new Object[0]);
               float size = 5.4F;
               float textW = hud.medium.width(letters, size);
               hud.text(hud.medium, letters, x - textW / 2.0F, HudContext.centered(0.0F, band, size), size, ColorUtil.fade(hud.secondary, fade));
            } else {
               hud.quad(x - 0.35F, band * 0.36F, x + 0.35F, band * 0.64F, ColorUtil.fade(base, 0.35F * fade));
            }
         }
      }

      hud.unclip();
      float marker = 3.2F;
      hud.g.getMatrices().pushMatrix();
      hud.g.getMatrices().translate(center, 1.8F);
      hud.g.getMatrices().rotate((float)Math.toRadians(45.0));
      hud.fill(-marker / 2.0F, -marker / 2.0F, marker, marker, 0.8F, hud.accent());
      hud.g.getMatrices().popMatrix();
      if (this.degrees.isOn()) {
         String text = Math.round(heading) % 360 + "\u00b0";
         float size = 5.2F;
         float textW = hud.medium.width(text, size);
         float pillY = band - 1.5F;
         float pillH = 7.0F;
         hud.fill(center - textW / 2.0F - 3.5F, pillY, textW + 7.0F, pillH, pillH / 2.0F, ColorUtil.withAlpha(hud.accent(), 184));
         hud.text(hud.medium, text, center - textW / 2.0F, HudContext.centered(pillY, pillH, size), size, -1);
      }
   }
}
