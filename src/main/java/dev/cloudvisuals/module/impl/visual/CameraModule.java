package dev.cloudvisuals.module.impl.visual;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.NumberSetting;

public final class CameraModule extends Module {
   private final NumberSetting hurtTilt = this.add(new NumberSetting("hurt_tilt", 0.0, 0.0, 100.0, 5.0, "cloudvisuals.unit.percent"));
   private final NumberSetting bobbing = this.add(new NumberSetting("bobbing", 100.0, 0.0, 100.0, 5.0, "cloudvisuals.unit.percent"));
   private final BoolSetting customFov = this.add(new BoolSetting("custom_fov", false));
   private final NumberSetting fov = this.add(new NumberSetting("fov", 90.0, 30.0, 110.0, 1.0, "cloudvisuals.unit.degrees"));
   private final NumberSetting dynamicFov = this.add(new NumberSetting("dynamic_fov", 100.0, 0.0, 100.0, 5.0, "cloudvisuals.unit.percent"));

   public CameraModule() {
      super("camera", Category.VISUALS, false);
      this.fov.visibleWhen(this.customFov::isOn);
   }

   @Override
   public int icon() {
      return 58678;
   }

   public double hurtTilt(double vanilla) {
      return this.isEnabled() ? vanilla * this.hurtTilt.get() / 100.0 : vanilla;
   }

   public float bobbing(float vanilla) {
      return this.isEnabled() ? vanilla * this.bobbing.floatValue() / 100.0F : vanilla;
   }

   public int fov(int vanilla) {
      return this.isEnabled() && this.customFov.isOn() ? this.fov.intValue() : vanilla;
   }

   public float fovModifier(float vanilla) {
      return this.isEnabled() ? 1.0F + (vanilla - 1.0F) * this.dynamicFov.floatValue() / 100.0F : vanilla;
   }
}
