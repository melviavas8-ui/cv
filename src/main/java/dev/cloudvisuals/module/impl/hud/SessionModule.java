package dev.cloudvisuals.module.impl.hud;

import dev.cloudvisuals.hud.Anchor;
import dev.cloudvisuals.hud.GameStats;
import dev.cloudvisuals.hud.HudContext;
import java.util.Locale;

public final class SessionModule extends InfoElement {
   public SessionModule() {
      super("session", Anchor.BOTTOM_RIGHT, -6.0F, -72.0F, false);
   }

   @Override
   public int icon() {
      return 58006;
   }

   @Override
   protected int valueIcon() {
      return 58006;
   }

   @Override
   protected boolean hasContent(HudContext hud) {
      return GameStats.sessionMillis() > 0L;
   }

   @Override
   protected String value(HudContext hud) {
      long millis = GameStats.sessionMillis();
      if (millis == 0L && hud.preview) {
         millis = 4980000L;
      }

      long hours = millis / 3600000L;
      long minutes = millis / 60000L % 60L;
      long seconds = millis / 1000L % 60L;
      return hours > 0L ? String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds) : String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
   }
}
