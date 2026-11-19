package dev.cloudvisuals.module.impl.visual;

import dev.cloudvisuals.mixin.OverlayTextureAccessor;
import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.ColorSetting;
import dev.cloudvisuals.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

public final class EntityColorsModule extends Module {
   private static final int VANILLA_HURT = -1291911168;
   private final BoolSetting hurt = this.add(new BoolSetting("hurt", true));
   private final ColorSetting hurtColor = this.add(new ColorSetting("hurt_color", -1470445318, true));
   private final BoolSetting glint = this.add(new BoolSetting("glint", true));
   private final ColorSetting glintColor = this.add(new ColorSetting("glint_color", -12991045, false));
   private int appliedHurt = -1291911168;

   public EntityColorsModule() {
      super("entity_colors", Category.VISUALS, false);
      this.hurtColor.visibleWhen(this.hurt::isOn);
      this.glintColor.visibleWhen(this.glint::isOn);
   }

   @Override
   public int icon() {
      return 58087;
   }

   @Override
   public void onTick() {
      this.applyHurtColor();
   }

   @Override
   protected void onDisable() {
      this.applyHurtColor();
   }

   @Override
   public void onConfigLoaded() {
      this.applyHurtColor();
   }

   private void applyHurtColor() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.gameRenderer != null) {
         int color = this.isEnabled() && this.hurt.isOn() ? this.hurtColor.color() : -1291911168;
         if (color != this.appliedHurt) {
            this.appliedHurt = color;
            NativeImageBackedTexture texture = ((OverlayTextureAccessor)mc.gameRenderer.getOverlayTexture()).cloudvisuals$texture();
            NativeImage pixels = texture.getImage();
            if (pixels != null) {
               int abgr = ColorUtil.alpha(color) << 24 | ColorUtil.blue(color) << 16 | ColorUtil.green(color) << 8 | ColorUtil.red(color);

               for (int y = 0; y < 8; y++) {
                  for (int x = 0; x < 16; x++) {
                     pixels.setColor(x, y, abgr);
                  }
               }

               texture.upload();
            }
         }
      }
   }

   public int glintColor() {
      return this.isEnabled() && this.glint.isOn() ? this.glintColor.color() : 0;
   }
}
