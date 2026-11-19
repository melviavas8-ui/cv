package dev.cloudvisuals.ui;

import dev.cloudvisuals.anim.Spring;
import dev.cloudvisuals.render.LiquidGlass;
import dev.cloudvisuals.render.Render2D;
import dev.cloudvisuals.render.Surfaces;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.style.Corners;
import dev.cloudvisuals.render.style.Glass;
import dev.cloudvisuals.render.style.Shadow;
import dev.cloudvisuals.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;

public final class ToggleSwitch {
   private static final Glass LENS = Glass.lens();
   private final Spring position;
   private boolean on;

   public ToggleSwitch(boolean on) {
      this.on = on;
      this.position = new Spring(on ? 1.0 : 0.0, 0.38, 0.68);
   }

   public void set(boolean on) {
      this.on = on;
      this.position.setTarget(on ? 1.0 : 0.0);
   }

   public boolean isOn() {
      return this.on;
   }

   public float progress() {
      return this.position.getFloat();
   }

   public void render(DrawContext g, float x, float y, float w, float h, Glass glass, float alpha) {
      this.render(g, x, y, w, h, glass, alpha, Theme.positive, Theme.positive, false);
   }

   public void render(DrawContext g, float x, float y, float w, float h, Glass glass, float alpha, int onLeft, int onRight, boolean glow) {
      float t = this.position.getFloat();
      float motion = Math.min((float)Math.abs(this.position.velocity()) / 6.0F, 1.0F);
      float on = Math.clamp(t, 0.0F, 1.0F);
      if (glow && on > 0.01F) {
         Surfaces.glow(g, x, y, w, h, Corners.themed(h / 2.0F), Shadow.glow(ColorUtil.withAlpha(onLeft, 102), 5.0F, 0.0F), alpha * on);
      }

      Render2D.rect(g, x, y, w, h, h / 2.0F, ColorUtil.fade(Controls.track(glass), alpha));
      if (on > 0.001F) {
         Render2D.gradientH(g, x, y, w, h, h / 2.0F, ColorUtil.fade(onLeft, alpha * on), ColorUtil.fade(onRight, alpha * on));
      }

      float inset = 2.0F;
      float knobH = h - inset * 2.0F;
      float baseW = knobH * 1.5F;
      float left = x + inset + baseW / 2.0F;
      float right = x + w - inset - baseW / 2.0F;
      float lensW = knobH * (1.5F + 0.45F * motion) * (1.0F + 0.2F * motion);
      float lensH = knobH * (1.0F + 0.35F * motion);
      float cx = Math.clamp(left + (right - left) * t, x + lensW / 2.0F - 1.0F, x + w - lensW / 2.0F + 1.0F);
      float cy = y + h / 2.0F;
      float kx = cx - lensW / 2.0F;
      float ky = cy - lensH / 2.0F;
      Render2D.shadow(g, kx, ky + 1.0F, lensW, lensH, lensH / 2.0F, 2.5F, ColorUtil.fade(939524096, alpha));
      if (motion > 0.02F) {
         LiquidGlass.shapes().add(kx, ky, lensW, lensH, lensH / 2.0F).refreshBackdrop().draw(g, LENS, 0.0F, alpha * motion);
      }

      Render2D.rect(g, kx, ky, lensW, lensH, lensH / 2.0F, ColorUtil.fade(-1, alpha * (1.0F - motion)));
   }
}
