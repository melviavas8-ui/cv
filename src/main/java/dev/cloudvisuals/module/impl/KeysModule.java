package dev.cloudvisuals.module.impl;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;

public final class KeysModule extends Module {
   public KeysModule() {
      super("keys", Category.INTERFACE, true);
   }

   @Override
   public int icon() {
      return 57988;
   }

   @Override
   public boolean isAlwaysOn() {
      return true;
   }
}
