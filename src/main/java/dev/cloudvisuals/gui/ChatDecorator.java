package dev.cloudvisuals.gui;

import dev.cloudvisuals.module.impl.visual.ChatModule;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.hud.ChatHud.Backend;
import net.minecraft.client.gui.hud.ChatHudLine.Visible;
import net.minecraft.client.gui.hud.MessageIndicator.Icon;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2f;

public final class ChatDecorator implements Backend {
   private static final Map<OrderedText, Long> SEEN = new WeakHashMap<>();
   private final Backend delegate;
   private final DrawContext graphics;
   private final ChatModule chat;
   private final List<Visible> lines;
   private final int tick;
   private final boolean focused;
   private final float opacity;
   private float panelTop = Float.MAX_VALUE;
   private float panelBottom = -Float.MAX_VALUE;
   private float panelRight = -Float.MAX_VALUE;
   private boolean panelDrawn;

   public ChatDecorator(Backend delegate, DrawContext graphics, ChatModule chat, List<Visible> lines, int tick, boolean focused, float opacity) {
      this.delegate = delegate;
      this.graphics = graphics;
      this.chat = chat;
      this.lines = lines;
      this.tick = tick;
      this.focused = focused;
      this.opacity = opacity;
   }

   public void updatePose(Consumer<Matrix3x2f> transformer) {
      this.delegate.updatePose(transformer);
   }

   public void fill(int x1, int y1, int x2, int y2, int color) {
      if (this.chat.glass() && x1 == -4) {
         this.panelTop = Math.min(this.panelTop, y1);
         this.panelBottom = Math.max(this.panelBottom, y2);
         this.panelRight = Math.max(this.panelRight, x2 - 4);
      } else {
         this.delegate.fill(x1, y1, x2, y2, color);
      }
   }

   public boolean text(int y, float opacity, OrderedText text) {
      this.drawPanel();
      if (!this.chat.animation()) {
         return this.delegate.text(y, opacity, text);
      }

      Visible line = this.find(text);
      long seen = SEEN.computeIfAbsent(text, key -> line != null && this.tick - line.addedTime() <= 20 ? System.currentTimeMillis() : 0L);
      float slide = seen == 0L ? 0.0F : this.chat.slide(seen);
      if (slide <= 0.001F) {
         return this.delegate.text(y, opacity, text);
      }

      float shift = -slide * 14.0F;
      this.delegate.updatePose(pose -> pose.translate(shift, 0.0F));
      boolean hovered = this.delegate.text(y, Math.max(0.02F, opacity * (1.0F - slide)), text);
      this.delegate.updatePose(pose -> pose.translate(-shift, 0.0F));
      return hovered;
   }

   public void indicator(int x1, int y1, int x2, int y2, float opacity, MessageIndicator indicator) {
      this.delegate.indicator(x1, y1, x2, y2, opacity, indicator);
   }

   public void indicatorIcon(int left, int bottom, boolean forceDraw, MessageIndicator indicator, Icon icon) {
      this.delegate.indicatorIcon(left, bottom, forceDraw, indicator, icon);
   }

   private void drawPanel() {
      if (!this.panelDrawn && this.chat.glass() && !(this.panelBottom < this.panelTop)) {
         this.panelDrawn = true;
         float alpha = 0.0F;

         for (Visible line : this.lines) {
            int age = this.tick - line.addedTime();
            if (this.focused || age < 200) {
               alpha = Math.max(alpha, this.focused ? 1.0F : visibility(age));
            }
         }

         if (!(alpha <= 0.01F)) {
            this.chat
               .drawPanel(this.graphics, -4.0F, this.panelTop - 2.0F, this.panelRight + 4.0F, this.panelBottom - this.panelTop + 3.0F, alpha * this.opacity);
         }
      }
   }

   private static float visibility(int age) {
      double d = MathHelper.clamp((1.0 - age / 200.0) * 10.0, 0.0, 1.0);
      return (float)(d * d);
   }

   private Visible find(OrderedText text) {
      for (Visible line : this.lines) {
         if (line.content() == text) {
            return line;
         }
      }

      return null;
   }
}
