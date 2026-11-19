package dev.cloudvisuals.module.impl;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.NumberSetting;

public final class NotificationsModule extends Module {
   private final BoolSetting toggles = this.add(new BoolSetting("toggles", true));
   private final NumberSetting duration = this.add(new NumberSetting("duration", 2.2, 0.8, 8.0, 0.1, "cloudvisuals.unit.seconds"));
   private final BoolSetting updates = this.add(new BoolSetting("updates", true));

   public NotificationsModule() {
      super("notifications", Category.INTERFACE, true);
   }

   @Override
   public int icon() {
      return 57433;
   }

   public boolean showToggles() {
      return this.toggles.isOn();
   }

   public boolean checkUpdates() {
      return this.updates.isOn();
   }

   public long durationMs() {
      return Math.round(this.duration.get() * 1000.0);
   }
}
