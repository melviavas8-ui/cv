package dev.cloudvisuals.module.impl.sound;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.setting.ActionSetting;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.NumberSetting;
import dev.cloudvisuals.setting.SoundSetting;
import dev.cloudvisuals.sound.SoundLibrary;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;

public final class HitSoundsModule extends Module {
   private final SoundSetting hit;
   private final NumberSetting hitVolume;
   private final NumberSetting pitch;
   private final NumberSetting pitchSpread;
   private final ActionSetting previewHit;
   private final SoundSetting crit;
   private final NumberSetting critVolume;
   private final ActionSetting previewCrit;
   private final SoundSetting kill;
   private final NumberSetting killVolume;
   private final ActionSetting previewKill;
   private final BoolSetting onlyPlayers;
   private final ActionSetting openFolder;
   private final Random random;
   private int pendingHit;
   private boolean pendingCrit;

   public HitSoundsModule() {
      super("hit_sounds", Category.SOUNDS, false);
      this.section("hit", 58035);
      this.hit = this.add(new SoundSetting("hit", "hit_pluck"));
      this.hitVolume = this.add(new NumberSetting("hit_volume", 80.0, 0.0, 100.0, 5.0, "cloudvisuals.unit.percent"));
      this.pitch = this.add(new NumberSetting("pitch", 100.0, 50.0, 200.0, 5.0, "cloudvisuals.unit.percent"));
      this.pitchSpread = this.add(new NumberSetting("pitch_spread", 8.0, 0.0, 40.0, 1.0, "cloudvisuals.unit.percent"));
      this.previewHit = this.add(new ActionSetting("preview_hit", 57660, () -> this.play(this.hit, this.hitVolume)));
      this.section("crit", 57780);
      this.crit = this.add(new SoundSetting("crit", "crit_zap"));
      this.critVolume = this.add(new NumberSetting("crit_volume", 80.0, 0.0, 100.0, 5.0, "cloudvisuals.unit.percent"));
      this.previewCrit = this.add(new ActionSetting("preview_crit", 57660, () -> this.play(this.crit, this.critVolume)));
      this.section("kill", 57889);
      this.kill = this.add(new SoundSetting("kill", "win_steel"));
      this.killVolume = this.add(new NumberSetting("kill_volume", 80.0, 0.0, 100.0, 5.0, "cloudvisuals.unit.percent"));
      this.previewKill = this.add(new ActionSetting("preview_kill", 57660, () -> this.play(this.kill, this.killVolume)));
      this.section("filter", 57764);
      this.onlyPlayers = this.add(new BoolSetting("only_players", false));
      this.openFolder = this.add(new ActionSetting("open_folder", 57927, HitSoundsModule::openFolder));
      this.random = new Random();
      this.pendingHit = -1;
   }

   @Override
   public int icon() {
      return 58036;
   }

   private boolean counts(Entity entity) {
      return entity != null && (!this.onlyPlayers.isOn() || entity instanceof PlayerEntity);
   }

   public void onDamage(Entity target) {
      if (this.isEnabled() && this.counts(target)) {
         this.pendingHit = target.getId();
      }
   }

   public void onCrit(Entity target) {
      if (this.isEnabled() && target != null && target.getId() == this.pendingHit) {
         this.pendingCrit = true;
      }
   }

   public void onKill(Entity entity) {
      if (this.isEnabled() && this.counts(entity)) {
         this.play(this.kill, this.killVolume);
      }
   }

   @Override
   public void onTick() {
      if (this.pendingHit >= 0) {
         if (this.pendingCrit && !"none".equals(this.crit.get())) {
            this.play(this.crit, this.critVolume);
         } else {
            this.play(this.hit, this.hitVolume);
         }

         this.pendingHit = -1;
         this.pendingCrit = false;
      }
   }

   private void play(SoundSetting sound, NumberSetting volume) {
      float spread = this.pitchSpread.floatValue() / 100.0F;
      float value = this.pitch.floatValue() / 100.0F * (1.0F + (this.random.nextFloat() * 2.0F - 1.0F) * spread);
      SoundLibrary.play(sound.get(), volume.floatValue() / 100.0F, value);
   }

   static void openFolder() {
      try {
         Files.createDirectories(SoundLibrary.FOLDER);
      } catch (IOException var1) {
      }

      Util.getOperatingSystem().open(SoundLibrary.FOLDER);
   }
}
