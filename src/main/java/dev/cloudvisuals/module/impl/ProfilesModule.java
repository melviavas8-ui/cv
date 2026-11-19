package dev.cloudvisuals.module.impl;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;

public final class ProfilesModule extends Module {
   public ProfilesModule() {
      super("profiles", Category.INTERFACE, true);
   }

   @Override
   public boolean isAlwaysOn() {
      return true;
   }

   @Override
   public boolean hasKeybind() {
      return false;
   }

   @Override
   public int icon() {
      return 57677;
   }
}
