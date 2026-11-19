package dev.cloudvisuals.gui.screen;

import dev.cloudvisuals.gui.menu.TextBuffer;
import dev.cloudvisuals.render.Surfaces;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.font.FontWeight;
import dev.cloudvisuals.render.font.Fonts;
import dev.cloudvisuals.render.font.SdfFont;
import dev.cloudvisuals.render.gpu.GuiDraw;
import dev.cloudvisuals.render.style.Corners;
import dev.cloudvisuals.render.style.Paint;
import dev.cloudvisuals.render.style.Shadow;
import dev.cloudvisuals.util.ColorUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;

public final class GlassSearchField extends ClickableWidget {
   public static final int WIDTH = 104;
   public static final int HEIGHT = 16;
   private static final float TEXT_SIZE = 7.6F;
   private static final float TEXT_LEFT = 18.0F;
   private static final float CLEAR_ROOM = 15.0F;
   private final TextBuffer buffer = new TextBuffer(40);
   private final String hint;
   private float focus;
   private float hover;
   private float filled;
   private float lastTime;

   public GlassSearchField(Text hint) {
      super(0, 0, 104, 16, hint);
      this.hint = hint.getString();
   }

   public String value() {
      return this.buffer.text();
   }

   public void setValue(String value) {
      this.buffer.set(value);
   }

   protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      float now = Theme.time();
      float rate = 1.0F - (float)Math.exp(-Math.min(0.1F, now - this.lastTime) * 18.0F);
      this.lastTime = now;
      this.focus = this.focus + ((this.isFocused() ? 1 : 0) - this.focus) * rate;
      this.hover = this.hover + ((this.isHovered() ? 1 : 0) - this.hover) * rate;
      this.filled = this.filled + ((this.buffer.isEmpty() ? 0 : 1) - this.filled) * rate;
      int accent = Theme.accent() | 0xFF000000;
      float x = this.getX();
      float y = this.getY();
      float w = this.width;
      float h = this.height;
      float middle = y + h / 2.0F;
      Corners pill = Corners.round(h / 2.0F);
      Surfaces.shadow(context, x, y + 2.0F, w, h, pill, new Shadow(1509949440, 10.0F, 0.0F, 0.0F, 3.0F), 1.0F);
      Surfaces.fill(context, x, y, w, h, pill, Paint.solid(ColorUtil.lerp(-770962158, -535422945, Math.max(this.hover, this.focus))), 1.0F);
      Surfaces.border(context, x, y, w, h, pill, 0.7F, ColorUtil.lerp(822083583, ColorUtil.withAlpha(accent, 230), this.focus), 1.0F);
      SdfFont icons = Fonts.icons();
      SdfFont font = Fonts.get(FontWeight.REGULAR);
      if (icons != null) {
         icons.drawIcon(context, 57681, x + 9.5F, middle, 8.0F, ColorUtil.lerp(-1711276033, accent, this.focus));
      }

      if (font != null) {
         String text = this.buffer.text();
         float room = w - 18.0F - 15.0F;
         float textY = middle - font.lineHeight(7.6F) / 2.0F;
         float caret = font.width(text.substring(0, this.buffer.cursor()), 7.6F);
         float shift = Math.max(0.0F, caret - (room - 2.0F));
         GuiDraw.scissor(context, (int)(x + 18.0F - 1.0F), (int)y, (int)(x + 18.0F + room), (int)(y + h));
         if (text.isEmpty()) {
            font.draw(context, this.hint, x + 18.0F, textY, 7.6F, -2130706433);
         } else {
            font.draw(context, text, x + 18.0F - shift, textY, 7.6F, -855049);
         }

         if (this.isFocused() && System.currentTimeMillis() / 530L % 2L == 0L) {
            Surfaces.fill(context, x + 18.0F + caret - shift, y + 4.0F, 0.8F, h - 8.0F, Corners.NONE, Paint.solid(accent), 1.0F);
         }

         context.disableScissor();
      }

      if (icons != null && this.filled > 0.02F) {
         boolean overClear = this.overClear(mouseX, mouseY);
         icons.drawIcon(context, 57476, x + w - 9.0F, middle, 8.0F, ColorUtil.withAlpha(overClear ? -1 : -4933952, Math.round(255.0F * this.filled)));
      }
   }

   private boolean overClear(double mouseX, double mouseY) {
      return !this.buffer.isEmpty()
         && mouseX >= this.getX() + this.width - 15.0F
         && mouseX < this.getX() + this.width
         && mouseY >= this.getY()
         && mouseY < this.getY() + this.height;
   }

   public void onClick(Click click, boolean doubled) {
      double mouseX = click.x();
      double mouseY = click.y();
      if (this.overClear(mouseX, mouseY)) {
         this.buffer.clear();
      } else {
         SdfFont font = Fonts.get(FontWeight.REGULAR);
         if (font != null) {
            String text = this.buffer.text();
            float caret = font.width(text.substring(0, this.buffer.cursor()), 7.6F);
            float room = this.width - 18.0F - 15.0F;
            float local = (float)mouseX - this.getX() - 18.0F + Math.max(0.0F, caret - (room - 2.0F));
            int best = 0;
            float bestDistance = Float.MAX_VALUE;

            for (int i = 0; i <= text.length(); i++) {
               float distance = Math.abs(font.width(text.substring(0, i), 7.6F) - local);
               if (distance < bestDistance) {
                  bestDistance = distance;
                  best = i;
               }
            }

            this.buffer.key(268, 0);

            for (int i = 0; i < best; i++) {
               this.buffer.key(262, 0);
            }
         }
      }
   }

   public boolean keyPressed(KeyInput input) {
      int key = input.key();
      int scancode = input.scancode();
      int modifiers = input.modifiers();
      if (!this.isFocused()) {
         return false;
      }

      this.buffer.key(key, modifiers);
      return true;
   }

   public boolean charTyped(CharInput input) {
      char character = (char)input.codepoint();
      int modifiers = input.modifiers();
      if (!this.isFocused()) {
         return false;
      }

      this.buffer.insert(String.valueOf(character));
      return true;
   }

   public void playDownSound(SoundManager soundManager) {
   }

   protected void appendClickableNarrations(NarrationMessageBuilder builder) {
      this.appendDefaultNarrations(builder);
   }
}
