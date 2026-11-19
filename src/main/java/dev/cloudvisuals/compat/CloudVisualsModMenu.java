package dev.cloudvisuals.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.cloudvisuals.gui.menu.MenuScreen;

public final class CloudVisualsModMenu implements ModMenuApi {
   public CloudVisualsModMenu() {
   }

   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return MenuScreen::new;
   }
}
