package dev.cloudvisuals.module.impl.hud;

import dev.cloudvisuals.hud.Anchor;
import dev.cloudvisuals.hud.HudContext;
import net.minecraft.client.MinecraftClient;

public final class FpsModule extends InfoElement {
   public FpsModule() {
      super("fps", Anchor.TOP_LEFT, 6.0F, 28.0F, true);
   }

   @Override
   public int icon() {
      return 57400;
   }

   @Override
   protected int valueIcon() {
      return 57400;
   }

   @Override
   public void onTick() {
      this.sample(MinecraftClient.getInstance().getCurrentFps());
   }

   @Override
   protected String value(HudContext hud) {
      return Integer.toString(hud.mc.getCurrentFps());
   }

   @Override
   protected String unit(HudContext hud) {
      return "fps";
   }
}
