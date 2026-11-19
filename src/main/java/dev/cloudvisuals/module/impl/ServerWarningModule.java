package dev.cloudvisuals.module.impl;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.notify.Notifications;
import dev.cloudvisuals.server.KnownServers;
import net.minecraft.client.resource.language.I18n;

public final class ServerWarningModule extends Module {
   public ServerWarningModule() {
      super("server_warning", Category.INTERFACE, true);
   }

   @Override
   public int icon() {
      return 57854;
   }

   public void onJoin() {
      if (this.isEnabled()) {
         KnownServers.whitelisted(KnownServers.currentHost())
            .ifPresent(
               server -> Notifications.show(
                  Notifications.Kind.WARNING,
                  I18n.translate("cloudvisuals.warning.whitelist.title", new Object[]{server.name()}),
                  I18n.translate("cloudvisuals.warning.whitelist.subtitle", new Object[0])
               )
            );
      }
   }
}
