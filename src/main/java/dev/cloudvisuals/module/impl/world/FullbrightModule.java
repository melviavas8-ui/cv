package dev.cloudvisuals.module.impl.world;

import dev.cloudvisuals.anim.Spring;
import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;

public final class FullbrightModule extends Module {
   private static final float MAX_GAMMA = 12.0F;
   private final NumberSetting strength = this.add(new NumberSetting("strength", 100.0, 10.0, 100.0, 5.0, "cloudvisuals.unit.percent"));
   private final Spring level = new Spring(0.0, 0.6, 1.0);

   public FullbrightModule() {
      super("fullbright", Category.WORLD, false);
   }

   @Override
   public int icon() {
      return 57720;
   }

   public float gamma(float vanilla) {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      boolean blocked = player != null && (player.hasStatusEffect(StatusEffects.BLINDNESS) || player.hasStatusEffect(StatusEffects.DARKNESS));
      float target = this.isEnabled() && !blocked ? this.strength.floatValue() / 100.0F : 0.0F;
      float t = Math.clamp(this.level.setTarget(target).getFloat(), 0.0F, 1.0F);
      return vanilla + (Math.max(vanilla, 12.0F) - vanilla) * t;
   }
}
