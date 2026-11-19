package dev.cloudvisuals.module.impl.visual;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.ColorSetting;
import dev.cloudvisuals.setting.NumberSetting;
import dev.cloudvisuals.util.ColorUtil;

public final class NametagsModule extends Module {
   private final NumberSetting scale = this.add(new NumberSetting("scale", 100.0, 50.0, 150.0, 5.0, "cloudvisuals.unit.percent"));
   private final ColorSetting background = this.add(new ColorSetting("background", 1427116052, true));
   private final BoolSetting shadow = this.add(new BoolSetting("shadow", true));
   private final BoolSetting self = this.add(new BoolSetting("self", true));

   public NametagsModule() {
      super("nametags", Category.VISUALS, false);
   }

   @Override
   public int icon() {
      return 57727;
   }

   public float scale() {
      return this.isEnabled() ? this.scale.floatValue() / 100.0F : 1.0F;
   }

   public int background(int vanilla) {
      return this.isEnabled() ? this.background.color() : vanilla;
   }

   public boolean shadow(boolean vanilla) {
      return this.isEnabled() ? this.shadow.isOn() : vanilla;
   }

   public boolean showSelf() {
      return this.isEnabled() && this.self.isOn();
   }

   static int opaque(int color) {
      return ColorUtil.withAlpha(color, 255);
   }
}
