package dev.cloudvisuals.gui;

import dev.cloudvisuals.anim.Spring;
import dev.cloudvisuals.gui.menu.MenuScreen;
import dev.cloudvisuals.render.LiquidGlass;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.font.FontWeight;
import dev.cloudvisuals.render.font.Fonts;
import dev.cloudvisuals.render.font.SdfFont;
import dev.cloudvisuals.render.style.Corners;
import dev.cloudvisuals.render.style.Glass;
import dev.cloudvisuals.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public final class CloudVisualsMenuButton extends ClickableWidget {
   private final Screen owner;
   private final Spring hover = new Spring(0.0, 0.2, 0.8);

   public CloudVisualsMenuButton(Screen owner, int x, int y, int width, int height) {
      super(x, y, width, height, Text.literal("Cloud Visuals"));
      this.owner = owner;
   }

   protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      SdfFont bold = Fonts.get(FontWeight.DISPLAY_BOLD);
      SdfFont icons = Fonts.icons();
      float h = Math.clamp(this.hover.setTarget(this.isSelected() ? 1.0 : 0.0).getFloat(), 0.0F, 1.2F);
      float grow = 1.0F + 0.06F * h;
      float w = this.getWidth() * grow;
      float height = this.getHeight() * grow;
      float x = this.getX() + (this.getWidth() - w) / 2.0F;
      float y = this.getY() + (this.getHeight() - height) / 2.0F;
      Glass glass = Theme.glass().copy();
      glass.bevel = 6.0F;
      LiquidGlass.draw(context, x, y, w, height, Corners.round(height / 2.0F), glass, 1.0F);
      if (bold != null && icons != null) {
         int label = glass.label;
         float size = height * 0.36F;
         float textW = bold.width("CLOUD VISUALS", size, 0.8F);
         float iconSize = height * 0.5F;
         float contentW = iconSize + 4.0F + textW;
         float left = this.getX() + (this.getWidth() - contentW) / 2.0F;
         icons.drawIcon(
            context,
            57525,
            left + iconSize / 2.0F,
            this.getY() + this.getHeight() / 2.0F,
            iconSize,
            ColorUtil.lerp(label, Theme.accent(), 0.4F + 0.6F * Math.min(1.0F, h))
         );
         bold.draw(context, "CLOUD VISUALS", left + iconSize + 4.0F, this.getY() + this.getHeight() / 2.0F - size * 0.62F, size, 0.8F, label);
      }
   }

   public void onClick(Click click, boolean doubled) {
      double mouseX = click.x();
      double mouseY = click.y();
      MinecraftClient.getInstance().setScreen(new MenuScreen(this.owner));
   }

   protected void appendClickableNarrations(NarrationMessageBuilder builder) {
      this.appendDefaultNarrations(builder);
   }
}
