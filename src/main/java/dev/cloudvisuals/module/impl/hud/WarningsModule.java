package dev.cloudvisuals.module.impl.hud;

import dev.cloudvisuals.hud.Anchor;
import dev.cloudvisuals.hud.HudContext;
import dev.cloudvisuals.hud.HudElement;
import dev.cloudvisuals.hud.Size;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.NumberSetting;
import dev.cloudvisuals.util.ColorUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;

public final class WarningsModule extends HudElement {
   private static final EquipmentSlot[] ARMOR = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   private static final float PILL_H = 16.0F;
   private static final float GAP = 3.0F;
   private static final float TEXT = 6.8F;
   private final BoolSetting armor = this.add(new BoolSetting("armor", true));
   private final NumberSetting armorPercent = this.add(new NumberSetting("armor_percent", 15.0, 5.0, 50.0, 5.0, "cloudvisuals.unit.percent"));
   private final BoolSetting tool = this.add(new BoolSetting("tool", true));
   private final NumberSetting toolPercent = this.add(new NumberSetting("tool_percent", 10.0, 5.0, 50.0, 5.0, "cloudvisuals.unit.percent"));
   private final BoolSetting effects = this.add(new BoolSetting("effects", true));
   private final NumberSetting effectSeconds = this.add(new NumberSetting("effect_seconds", 5.0, 2.0, 30.0, 1.0, "cloudvisuals.unit.seconds"));
   private List<String> warnings = List.of();

   public WarningsModule() {
      super("warnings", true, Anchor.CENTER, 0.0F, -46.0F);
   }

   @Override
   public int icon() {
      return 57747;
   }

   @Override
   protected boolean drawsBackground() {
      return false;
   }

   @Override
   protected float defaultRoundness() {
      return 1.0F;
   }

   @Override
   public void onTick() {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null) {
         this.warnings = List.of();
      } else {
         List<String> next = new ArrayList<>();
         if (this.armor.isOn()) {
            for (EquipmentSlot slot : ARMOR) {
               ItemStack stack = player.getEquippedStack(slot);
               int left = percentLeft(stack);
               if (left >= 0 && left <= this.armorPercent.intValue()) {
                  next.add(I18n.translate("cloudvisuals.hud.warnings.armor", new Object[]{stack.getName().getString(), left}));
               }
            }
         }

         if (this.tool.isOn()) {
            ItemStack hand = player.getMainHandStack();
            int left = percentLeft(hand);
            if (left >= 0 && left <= this.toolPercent.intValue()) {
               next.add(I18n.translate("cloudvisuals.hud.warnings.tool", new Object[]{hand.getName().getString(), left}));
            }
         }

         if (this.effects.isOn()) {
            for (StatusEffectInstance instance : player.getStatusEffects()) {
               if (!instance.isInfinite()
                  && instance.getDuration() > 0
                  && ((StatusEffect)instance.getEffectType().value()).getCategory() == StatusEffectCategory.BENEFICIAL) {
                  int seconds = instance.getDuration() / 20;
                  if (seconds <= this.effectSeconds.intValue()) {
                     next.add(
                        I18n.translate(
                           "cloudvisuals.hud.warnings.effect",
                           new Object[]{((StatusEffect)instance.getEffectType().value()).getName().getString(), String.format(Locale.ROOT, "0:%02d", seconds)}
                        )
                     );
                  }
               }
            }
         }

         this.warnings = next;
      }
   }

   private static int percentLeft(ItemStack stack) {
      return !stack.isEmpty() && stack.isDamageable() ? Math.round(100.0F * (1.0F - (float)stack.getDamage() / stack.getMaxDamage())) : -1;
   }

   private List<String> shown(HudContext hud) {
      return this.warnings.isEmpty() && hud.preview
         ? List.of(
            I18n.translate("cloudvisuals.hud.warnings.armor", new Object[]{I18n.translate("item.minecraft.diamond_helmet", new Object[0]), 12}),
            I18n.translate("cloudvisuals.hud.warnings.effect", new Object[]{I18n.translate("effect.minecraft.strength", new Object[0]), "0:04"})
         )
         : this.warnings;
   }

   @Override
   protected boolean hasContent(HudContext hud) {
      return !this.warnings.isEmpty();
   }

   @Override
   protected Size measure(HudContext hud) {
      List<String> list = this.shown(hud);
      float width = 0.0F;

      for (String text : list) {
         width = Math.max(width, pillWidth(hud, text));
      }

      return new Size(Math.max(width, 40.0F), Math.max(16.0F, list.size() * 19.0F - 3.0F));
   }

   private static float pillWidth(HudContext hud, String text) {
      return 22.0F + hud.medium.width(text, 6.8F) + 9.0F;
   }

   @Override
   protected void render(HudContext hud, float w, float h) {
      List<String> list = this.shown(hud);
      float pulse = 0.6F + 0.4F * (float)Math.sin(System.currentTimeMillis() / 180.0);
      float y = 0.0F;

      for (String text : list) {
         float pillW = pillWidth(hud, text);
         float x = hud.alignRight() ? w - pillW : (hud.alignCenter() ? (w - pillW) / 2.0F : 0.0F);
         hud.background(x, y, pillW, 16.0F);
         hud.fill(x + 1.0F, y + 1.0F, pillW - 2.0F, 14.0F, 7.0F, ColorUtil.withAlpha(Theme.warning, Math.round(34.0F * pulse)));
         hud.icon(57747, x + 8.0F + 5.0F, y + 8.0F, 8.5F, ColorUtil.lerp(Theme.warning, -1, 0.25F * (1.0F - pulse)));
         hud.text(hud.medium, text, x + 8.0F + 14.0F, HudContext.centered(y, 16.0F, 6.8F), 6.8F, hud.label);
         y += 19.0F;
      }
   }
}
