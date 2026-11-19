package dev.cloudvisuals.module.impl.visual;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.setting.NumberSetting;

public final class ColorGradingModule extends Module {
   private final NumberSetting saturation = this.add(new NumberSetting("saturation", 108.0, 0.0, 200.0, 5.0, "cloudvisuals.unit.percent"));
   private final NumberSetting contrast = this.add(new NumberSetting("contrast", 104.0, 50.0, 150.0, 2.0, "cloudvisuals.unit.percent"));
   private final NumberSetting vibrance = this.add(new NumberSetting("vibrance", 15.0, 0.0, 100.0, 5.0, "cloudvisuals.unit.percent"));
   private final NumberSetting exposure = this.add(new NumberSetting("exposure", 100.0, 60.0, 140.0, 2.0, "cloudvisuals.unit.percent"));
   private final NumberSetting warmth = this.add(new NumberSetting("warmth", 0.0, -100.0, 100.0, 5.0, "cloudvisuals.unit.percent"));

   public ColorGradingModule() {
      super("color_grading", Category.VISUALS, false);
   }

   @Override
   public int icon() {
      return 57501;
   }

   public float saturation() {
      return this.saturation.floatValue() / 100.0F;
   }

   public float contrast() {
      return this.contrast.floatValue() / 100.0F;
   }

   public float vibrance() {
      return this.vibrance.floatValue() / 100.0F;
   }

   public float exposure() {
      return this.exposure.floatValue() / 100.0F;
   }

   public float warmth() {
      return this.warmth.floatValue() / 100.0F;
   }
}
