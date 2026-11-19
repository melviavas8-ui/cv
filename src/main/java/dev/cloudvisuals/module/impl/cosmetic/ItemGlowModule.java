package dev.cloudvisuals.module.impl.cosmetic;

import dev.cloudvisuals.render.CloudVisualsRenderTypes;
import dev.cloudvisuals.render.GlowEngine;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.world.ItemProbe;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.NumberSetting;
import dev.cloudvisuals.util.Weapons;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public final class ItemGlowModule extends CosmeticModule {
   private final NumberSetting strength = this.add(new NumberSetting("strength", 50.0, 10.0, 100.0, 5.0, "cloudvisuals.unit.percent"));
   private final BoolSetting pulse = this.add(new BoolSetting("pulse", true));
   private final BoolSetting offhand = this.add(new BoolSetting("offhand", true));
   private final BoolSetting onlyWeapons = this.add(new BoolSetting("only_weapons", false));
   private final ItemProbe probe = new ItemProbe();

   public ItemGlowModule() {
      super("item_glow");
   }

   @Override
   public int icon() {
      return 57922;
   }

   public boolean wants(ItemStack stack, boolean mainHand) {
      if (this.isEnabled() && !stack.isEmpty() && GlowEngine.active()) {
         return !mainHand && !this.offhand.isOn() ? false : !this.onlyWeapons.isOn() || Weapons.is(stack);
      } else {
         return false;
      }
   }

   public OrderedRenderCommandQueue silhouette(OrderedRenderCommandQueue target, boolean mainHand) {
      float time = Theme.time();
      float wave = this.pulse.isOn() ? 0.82F + 0.18F * MathHelper.sin(time * 2.4F + (mainHand ? 0.0F : 1.7F)) : 1.0F;
      return this.probe
         .silhouette(target, CloudVisualsRenderTypes.ITEM_SILHOUETTE, this.color(mainHand ? 0.0F : 0.5F, time, this.strength.floatValue() / 100.0F * wave));
   }
}
