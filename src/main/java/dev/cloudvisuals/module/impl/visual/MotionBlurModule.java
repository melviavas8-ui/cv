package dev.cloudvisuals.module.impl.visual;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;

public final class MotionBlurModule extends Module {
   private final NumberSetting strength = this.add(new NumberSetting("strength", 45.0, 5.0, 85.0, 5.0, "cloudvisuals.unit.percent"));

   public MotionBlurModule() {
      super("motion_blur", Category.VISUALS, false);
   }

   @Override
   public int icon() {
      return 57776;
   }

   public float strength() {
      float fps = Math.max(10, MinecraftClient.getInstance().getCurrentFps());
      return (float)Math.min(0.95, Math.pow(this.strength.floatValue() / 100.0F, 60.0 / fps));
   }
}
