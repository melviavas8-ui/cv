package dev.cloudvisuals.module;

import java.util.Locale;
import net.minecraft.client.resource.language.I18n;

public enum Category {
   INTERFACE(58406),
   HUD(57791),
   VISUALS(58199),
   WORLD(57576),
   COSMETICS(57922),
   SOUNDS(58714);

   private final int icon;

   Category(int icon) {
      this.icon = icon;
   }

   public String id() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public String displayName() {
      return I18n.translate("cloudvisuals.category." + this.id(), new Object[0]);
   }

   public int icon() {
      return this.icon;
   }
}
