package dev.cloudvisuals.module.impl.visual;

import dev.cloudvisuals.looks.Look;
import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.notify.Notifications;
import dev.cloudvisuals.setting.TextSetting;
import java.util.Locale;
import net.minecraft.client.resource.language.I18n;

public final class LooksModule extends Module {
   private final TextSetting last = this.add(new TextSetting("last", "", 32)).visibleWhen(() -> false);

   public LooksModule() {
      super("looks", Category.VISUALS, true);
   }

   @Override
   public boolean isAlwaysOn() {
      return true;
   }

   @Override
   public int icon() {
      return 58783;
   }

   public void apply(Look look, boolean notify) {
      look.apply();
      this.last.set(look.name());
      if (notify) {
         String name = I18n.translate("cloudvisuals.look." + look.name().toLowerCase(Locale.ROOT), new Object[0]);
         Notifications.show(
            Notifications.Kind.SUCCESS,
            I18n.translate("cloudvisuals.look.applied", new Object[]{name}),
            I18n.translate("cloudvisuals.look.applied.hint", new Object[0])
         );
      }
   }

   public Look last() {
      for (Look look : Look.values()) {
         if (look.name().equals(this.last.get())) {
            return look;
         }
      }

      return null;
   }
}
