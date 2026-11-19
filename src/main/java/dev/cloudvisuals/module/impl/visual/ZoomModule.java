package dev.cloudvisuals.module.impl.visual;

import dev.cloudvisuals.anim.Spring;
import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.KeySetting;
import dev.cloudvisuals.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public final class ZoomModule extends Module {
   private final KeySetting hold = this.add(new KeySetting("hold", 67));
   private final NumberSetting power = this.add(new NumberSetting("power", 4.0, 1.5, 12.0, 0.5, "cloudvisuals.unit.times"));
   private final BoolSetting wheel = this.add(new BoolSetting("wheel", true));
   private final BoolSetting smooth = this.add(new BoolSetting("smooth", true));
   private final BoolSetting slowMouse = this.add(new BoolSetting("slow_mouse", true));
   private final Spring amount = new Spring(0.0, 0.26, 0.92);
   private float wheelPower;
   private boolean held;

   public ZoomModule() {
      super("zoom", Category.VISUALS, true);
   }

   @Override
   public int icon() {
      return 57782;
   }

   @Override
   public boolean hasKeybind() {
      return false;
   }

   private boolean keyDown() {
      MinecraftClient mc = MinecraftClient.getInstance();
      int key = this.hold.get();
      return this.isEnabled() && key != -1 && mc.currentScreen == null && mc.player != null ? InputUtil.isKeyPressed(mc.getWindow(), key) : false;
   }

   public float factor() {
      boolean down = this.keyDown();
      if (down && !this.held) {
         this.wheelPower = this.power.floatValue();
      }

      this.held = down;
      float target = down ? 1.0F : 0.0F;
      float t = this.smooth.isOn() ? Math.clamp(this.amount.setTarget(target).getFloat(), 0.0F, 1.0F) : target;
      if (!this.smooth.isOn()) {
         this.amount.snap(target);
      }

      return t <= 0.001F ? 1.0F : (float)Math.pow(Math.max(1.5F, this.wheelPower), t);
   }

   public boolean scroll(double amountScrolled) {
      if (this.wheel.isOn() && this.held && amountScrolled != 0.0) {
         this.wheelPower = Math.clamp(this.wheelPower * (amountScrolled > 0.0 ? 1.18F : 0.84745765F), 1.5F, 24.0F);
         return true;
      } else {
         return false;
      }
   }

   public double sensitivity(double vanilla) {
      if (!this.slowMouse.isOn()) {
         return vanilla;
      }

      float factor = !this.held && !(this.amount.getFloat() > 0.01F) ? 1.0F : this.factorNow();
      if (factor <= 1.001F) {
         return vanilla;
      }

      double base = (vanilla * 0.6 + 0.2) / Math.cbrt(factor);
      return (base - 0.2) / 0.6;
   }

   private float factorNow() {
      float t = Math.clamp(this.amount.getFloat(), 0.0F, 1.0F);
      return t <= 0.001F ? 1.0F : (float)Math.pow(Math.max(1.5F, this.wheelPower), t);
   }
}
