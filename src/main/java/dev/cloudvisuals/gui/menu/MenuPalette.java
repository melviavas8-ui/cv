package dev.cloudvisuals.gui.menu;

import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.style.Glass;
import dev.cloudvisuals.util.ColorUtil;

public record MenuPalette(
   boolean light,
   Glass glass,
   int text,
   int textDim,
   int textFaint,
   int accent,
   int accentText,
   int accentSoft,
   int group,
   int groupHover,
   int field,
   int border,
   int divider,
   int track,
   int knob,
   int popupTint,
   int danger,
   int positive,
   int warning
) {
   public MenuPalette {
   }

   public static MenuPalette current() {
      Glass glass = Theme.glass();
      boolean dark = glass.labelBrightness() > 0.5F;
      int label = glass.label;
      int accent = Theme.accent();
      int accentText;
      if (dark) {
         accentText = ColorUtil.lerp(accent, -1, 0.3F);
      } else {
         float luminance = luminance(accent);
         float darken = luminance > 0.3F ? Math.min((luminance - 0.3F) / luminance, 0.7F) : 0.0F;
         accentText = ColorUtil.lerp(accent, -16777216, darken);
      }

      int track = !dark
         ? ColorUtil.lerp(ColorUtil.withAlpha(9082022, 60), accent, 0.22F)
         : ColorUtil.withAlpha(7895167, Math.round(40.0F + 40.0F * glass.labelBrightness()));
      return new MenuPalette(
         !dark,
         glass,
         label,
         ColorUtil.fade(label, 0.62F),
         ColorUtil.fade(label, 0.4F),
         accent,
         accentText,
         ColorUtil.withAlpha(accent, dark ? 68 : 92),
         dark ? 318767103 : ColorUtil.lerp(1308622847, accent, 0.06F),
         dark ? 520093695 : 1895825407,
         dark ? 385875967 : ColorUtil.lerp(1560281087, accent, 0.05F),
         dark ? 536870911 : 1308622847,
         ColorUtil.fade(label, 0.08F),
         track,
         -1,
         dark ? -938208231 : -638257928,
         Theme.danger,
         Theme.positive,
         Theme.warning
      );
   }

   public int textOnAccent() {
      return luminance(this.accent) > 0.72F ? -15658473 : -1;
   }

   public int hover(float amount) {
      return ColorUtil.withAlpha(this.accent, Math.clamp(Math.round((this.light ? 18 : 14) * amount), 0, 255));
   }

   private static float luminance(int color) {
      return (ColorUtil.red(color) * 0.2126F + ColorUtil.green(color) * 0.7152F + ColorUtil.blue(color) * 0.0722F) / 255.0F;
   }
}
