package dev.cloudvisuals.module.impl.hud;

import dev.cloudvisuals.anim.Spring;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

public final class GlassHotbarModule extends Module {
   private static final float SLOT = 20.0F;
   private final BoolSetting stretch = this.add(new BoolSetting("stretch", true));
   private final BoolSetting accentSelection = this.add(new BoolSetting("accent", true));
   private final Spring selection = new Spring(0.0, 0.26, 0.74);
   private boolean placed;

   public GlassHotbarModule() {
      super("glass_hotbar", Category.HUD, false);
   }

   @Override
   public int icon() {
      return 58412;
   }

   public void renderBackground(DrawContext g, PlayerEntity player) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (!mc.options.hudHidden) {
         Backdrop.capture();
         float center = g.getScaledWindowWidth() / 2.0F;
         float y = g.getScaledWindowHeight() - 22;
         float x = center - 91.0F;
         float w = 182.0F;
         float h = 22.0F;
         Glass glass = Theme.glass().copy();
         glass.bevel = 7.0F;
         Corners corners = Corners.themed(10.0F);
         Surfaces.shadow(g, x, y + 1.5F, w, h, corners, new Shadow(1342177280, 7.0F, 0.0F, 0.0F, 2.0F), 1.0F);
         LiquidGlass.draw(g, x, y, w, h, corners, glass, 1.0F);
         ItemStack offhand = player.getOffHandStack();
         if (!offhand.isEmpty()) {
            float offX = player.getMainArm().getOpposite() == Arm.LEFT ? x - 29.0F : x + w + 7.0F;
            Surfaces.shadow(g, offX, y + 1.5F, 22.0F, h, corners, new Shadow(1342177280, 7.0F, 0.0F, 0.0F, 2.0F), 1.0F);
            LiquidGlass.draw(g, offX, y, 22.0F, h, corners, glass, 1.0F);
         }

         int slot = player.getInventory().getSelectedSlot();
         if (!this.placed) {
            this.selection.snap(slot);
            this.placed = true;
         }

         float position = this.selection.setTarget(slot).getFloat();
         float speed = this.stretch.isOn() ? Math.min(8.0F, (float)Math.abs(this.selection.velocity()) * 0.9F) : 0.0F;
         float lensW = 20.0F + speed;
         float lensH = 20.0F - speed * 0.25F;
         float lensX = x + 1.0F + position * 20.0F + (20.0F - lensW) / 2.0F;
         float lensY = y + 1.0F + (20.0F - lensH) / 2.0F;
         int accent = Theme.accent();
         Glass lens = Glass.lens();
         lens.refraction = 5.0F;
         lens.magnify = 1.12F;
         lens.tint = Paint.solid(this.accentSelection.isOn() ? ColorUtil.withAlpha(accent, 80) : 822083583);
         LiquidGlass.shapes().add(lensX, lensY, lensW, lensH, Corners.themed(8.0F)).refreshBackdrop().draw(g, lens, 0.0F, 1.0F);
         Surfaces.border(
            g, lensX, lensY, lensW, lensH, Corners.themed(8.0F), 1.0F, this.accentSelection.isOn() ? ColorUtil.lerp(accent, -1, 0.3F) : -1325400065, 1.0F
         );
      }
   }
}
