package dev.cloudvisuals.demo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class DemoScreen extends Screen {
   private final Screen parent;

   public DemoScreen(Screen parent) {
      super(Text.literal("Cloud Visuals"));
      this.parent = parent;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      this.renderPanoramaBackground(context, deltaTicks);
      Showcase.draw(context, this.width, this.height);
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   public boolean shouldPause() {
      return false;
   }
}
