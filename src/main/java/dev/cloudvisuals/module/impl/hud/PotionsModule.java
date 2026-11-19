package dev.cloudvisuals.module.impl.hud;

import dev.cloudvisuals.hud.Anchor;
import dev.cloudvisuals.hud.HudContext;
import dev.cloudvisuals.hud.HudElement;
import dev.cloudvisuals.hud.Size;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.font.SdfFont;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.ChoiceSetting;
import dev.cloudvisuals.util.ColorUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;

public final class PotionsModule extends HudElement {
   private static final float ROW = 20.0F;
   private static final float PAD = 5.0F;
   private static final float BLINK_TICKS = 200.0F;
   private final ChoiceSetting<PotionsModule.Layout> layout = this.add(new ChoiceSetting<>("layout", PotionsModule.Layout.LIST));
   private final BoolSetting hideVanilla = this.add(new BoolSetting("hide_vanilla", true));
   private final BoolSetting blink = this.add(new BoolSetting("blink", true));
   private final Map<RegistryEntry<StatusEffect>, Integer> longest = new HashMap<>();

   public PotionsModule() {
      super("potions", false, Anchor.RIGHT, -6.0F, 0.0F);
   }

   @Override
   public int icon() {
      return 57557;
   }

   public boolean hidesVanilla() {
      return this.isEnabled() && this.hideVanilla.isOn();
   }

   @Override
   public void onTick() {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null) {
         this.longest.clear();
      } else {
         this.longest.keySet().removeIf(effect -> !player.hasStatusEffect(effect));

         for (StatusEffectInstance instance : player.getStatusEffects()) {
            int duration = instance.getDuration();
            Integer known = this.longest.get(instance.getEffectType());
            if (known == null || duration > known) {
               this.longest.put(instance.getEffectType(), duration);
            }
         }
      }
   }

   private List<PotionsModule.Entry> entries(HudContext hud) {
      List<PotionsModule.Entry> entries = new ArrayList<>();
      ClientPlayerEntity player = hud.mc.player;
      if (player != null) {
         for (StatusEffectInstance instance : player.getStatusEffects()) {
            if (instance.shouldShowIcon()) {
               int max = Math.max(1, this.longest.getOrDefault(instance.getEffectType(), instance.getDuration()));
               entries.add(
                  new PotionsModule.Entry(
                     instance.getEffectType(), instance.getAmplifier(), instance.getDuration(), instance.isInfinite(), (float)instance.getDuration() / max
                  )
               );
            }
         }

         entries.sort(
            Comparator.<PotionsModule.Entry, Boolean>comparing(entry -> ((StatusEffect)entry.effect().value()).getCategory() != StatusEffectCategory.BENEFICIAL)
               .thenComparing(entry -> -entry.duration())
         );
      }

      if (entries.isEmpty() && hud.preview) {
         entries.add(new PotionsModule.Entry(StatusEffects.SPEED, 1, 1800, false, 0.5F));
         entries.add(new PotionsModule.Entry(StatusEffects.STRENGTH, 0, 160, false, 0.08F));
         entries.add(new PotionsModule.Entry(StatusEffects.FIRE_RESISTANCE, 0, 8640, false, 0.9F));
      }

      return entries;
   }

   @Override
   protected boolean hasContent(HudContext hud) {
      return !this.entries(hud).isEmpty();
   }

   private static String name(PotionsModule.Entry entry) {
      String name = ((StatusEffect)entry.effect().value()).getName().getString();
      return entry.amplifier() > 0 ? name + " " + I18n.translate("enchantment.level." + (entry.amplifier() + 1), new Object[0]) : name;
   }

   private static float timeWidth(SdfFont font, PotionsModule.Entry entry, float size) {
      return entry.infinite() ? size * 1.3F : font.width(time(entry), size);
   }

   private static void drawTime(HudContext hud, SdfFont font, PotionsModule.Entry entry, float x, float y, float size, int color) {
      if (entry.infinite()) {
         hud.icon(57831, x + size * 0.65F, y + size * 0.6F, size * 1.25F, color);
      } else {
         hud.text(font, time(entry), x, y, size, color);
      }
   }

   private static String time(PotionsModule.Entry entry) {
      int seconds = entry.duration() / 20;
      return seconds >= 3600
         ? String.format(Locale.ROOT, "%d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
         : String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
   }

   private static int color(PotionsModule.Entry entry, int accent) {
      return switch (((StatusEffect)entry.effect().value()).getCategory()) {
         case BENEFICIAL -> accent;
         case HARMFUL -> Theme.danger;
         case NEUTRAL -> Theme.warning;
         default -> throw new MatchException(null, null);
      };
   }

   @Override
   protected Size measure(HudContext hud) {
      List<PotionsModule.Entry> entries = this.entries(hud);
      if (this.layout.get() == PotionsModule.Layout.COMPACT) {
         return new Size(10.0F + entries.size() * 24 - 4.0F, 34.0F);
      }

      float width = 0.0F;

      for (PotionsModule.Entry entry : entries) {
         width = Math.max(width, Math.max(hud.semibold.width(name(entry), 6.8F), timeWidth(hud.regular, entry, 6.2F)));
      }

      return new Size(32.0F + width + 4.0F, 10.0F + entries.size() * 20.0F - 2.0F);
   }

   @Override
   protected void render(HudContext hud, float w, float h) {
      List<PotionsModule.Entry> entries = this.entries(hud);
      int accent = hud.accent();
      int track = ColorUtil.fade(hud.label, 0.12F);
      float base = hud.alpha;
      boolean compact = this.layout.get() == PotionsModule.Layout.COMPACT;
      float x = 5.0F;
      float y = 4.0F;

      for (PotionsModule.Entry entry : entries) {
         float fade = 1.0F;
         if (this.blink.isOn() && !entry.infinite() && entry.duration() < 200.0F) {
            float urgency = 1.0F - entry.duration() / 200.0F;
            fade = 0.55F
               + 0.45F
                  * (0.5F + 0.5F * (float)Math.cos(hud.mc.world == null ? 0.0 : ((float)hud.mc.world.getTime() + hud.partialTick) * (0.25F + urgency * 0.5F)));
         }

         hud.alpha = base * fade;
         int color = color(entry, accent);
         float centerX = compact ? x + 10.0F : 15.0F;
         float centerY = compact ? 14.0F : y + 10.0F - 1.0F;
         hud.ring(centerX, centerY, 9.0F, 1.6F, entry.infinite() ? 1.0F : entry.progress(), color, track);
         hud.effect(entry.effect(), centerX - 6.5F, centerY - 6.5F, 13.0F);
         if (compact) {
            float size = 5.6F;
            drawTime(hud, hud.medium, entry, centerX - timeWidth(hud.medium, entry, size) / 2.0F, 26.0F, size, hud.secondary);
            x += 24.0F;
         } else {
            float textX = 29.0F;
            hud.text(hud.semibold, name(entry), textX, y + 2.5F, 6.8F, hud.label);
            drawTime(hud, hud.regular, entry, textX, y + 11.0F, 6.2F, ColorUtil.lerp(hud.secondary, color, 0.35F));
            y += 20.0F;
         }
      }

      hud.alpha = base;
   }

   private record Entry(RegistryEntry<StatusEffect> effect, int amplifier, int duration, boolean infinite, float progress) {
      private Entry {
      }
   }

   public enum Layout {
      LIST,
      COMPACT;

      Layout() {
      }
   }
}
