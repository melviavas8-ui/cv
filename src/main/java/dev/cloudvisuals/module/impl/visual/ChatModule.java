package dev.cloudvisuals.module.impl.visual;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.render.Backdrop;
import dev.cloudvisuals.render.LiquidGlass;
import dev.cloudvisuals.render.Surfaces;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.style.Corners;
import dev.cloudvisuals.render.style.Glass;
import dev.cloudvisuals.render.style.Paint;
import dev.cloudvisuals.render.style.Shadow;
import dev.cloudvisuals.setting.BoolSetting;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ChatModule extends Module {
   private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
   private static final long SLIDE_MS = 260L;
   private final BoolSetting animation = this.add(new BoolSetting("animation", true));
   private final BoolSetting timestamps = this.add(new BoolSetting("timestamps", true));
   private final BoolSetting merge = this.add(new BoolSetting("merge", true));
   private final BoolSetting glass = this.add(new BoolSetting("glass", true));

   public ChatModule() {
      super("chat", Category.VISUALS, false);
   }

   @Override
   public int icon() {
      return 57623;
   }

   public boolean merge() {
      return this.isEnabled() && this.merge.isOn();
   }

   public boolean glass() {
      return this.isEnabled() && this.glass.isOn();
   }

   public boolean animation() {
      return this.isEnabled() && this.animation.isOn();
   }

   public Text decorate(Text message) {
      return (Text)(this.isEnabled() && this.timestamps.isOn()
         ? Text.literal(LocalTime.now().format(TIME) + " ").formatted(Formatting.DARK_GRAY).append(message)
         : message);
   }

   public float slide(long firstSeen) {
      float t = Math.clamp((float)(System.currentTimeMillis() - firstSeen) / 260.0F, 0.0F, 1.0F);
      float eased = 1.0F - (float)Math.pow(1.0F - t, 3.0);
      return 1.0F - eased;
   }

   public void drawPanel(DrawContext g, float x, float y, float w, float h, float alpha) {
      if (!(alpha <= 0.01F) && !(h <= 0.0F)) {
         Backdrop.capture();
         Corners corners = Corners.themed(9.0F);
         Glass panel = Theme.glass().copy();
         panel.tint = Paint.solid(-1945235950);
         panel.bevel = 6.0F;
         Surfaces.shadow(g, x, y + 1.5F, w, h, corners, new Shadow(1073741824, 6.0F, 0.0F, 0.0F, 2.0F), alpha);
         LiquidGlass.draw(g, x, y, w, h, corners, panel, alpha);
      }
   }
}
