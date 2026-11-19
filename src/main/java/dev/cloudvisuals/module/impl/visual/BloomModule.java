package dev.cloudvisuals.module.impl.visual;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.setting.NumberSetting;

public final class BloomModule extends Module {
   private final NumberSetting strength = this.add(new NumberSetting("strength", 100.0, 0.0, 250.0, 5.0, "cloudvisuals.unit.percent"));
   private final NumberSetting halo = this.add(new NumberSetting("halo", 100.0, 0.0, 250.0, 5.0, "cloudvisuals.unit.percent"));
   private final NumberSetting spread = this.add(new NumberSetting("spread", 100.0, 50.0, 200.0, 5.0, "cloudvisuals.unit.percent"));
   private final NumberSetting saturation = this.add(new NumberSetting("saturation", 115.0, 0.0, 200.0, 5.0, "cloudvisuals.unit.percent"));

   public BloomModule() {
      super("bloom", Category.VISUALS, true);
   }

   @Override
   public int icon() {
      return 57720;
   }

   public float strength() {
      return this.strength.floatValue() / 100.0F;
   }

   public float halo() {
      return this.halo.floatValue() / 100.0F;
   }

   public float spread() {
      return 1.15F * this.spread.floatValue() / 100.0F;
   }

   public float saturation() {
      return this.saturation.floatValue() / 100.0F;
   }
}
