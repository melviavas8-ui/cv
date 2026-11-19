package dev.cloudvisuals.module;

import dev.cloudvisuals.config.ConfigManager;
import dev.cloudvisuals.notify.Notifications;
import dev.cloudvisuals.setting.KeySetting;
import dev.cloudvisuals.setting.Setting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.resource.language.I18n;

public abstract class Module {
   private final String id;
   private final Category category;
   private final boolean enabledByDefault;
   private final List<Setting<?>> settings = new ArrayList<>();
   private final KeySetting key;
   private final Map<String, Integer> sectionIcons = new HashMap<>();
   private String section = "";
   private boolean enabled;
   private boolean favorite;

   protected Module(String id, Category category, boolean enabledByDefault) {
      this(id, category, enabledByDefault, -1);
   }

   protected Module(String id, Category category, boolean enabledByDefault, int defaultKey) {
      this.id = id;
      this.category = category;
      this.enabledByDefault = enabledByDefault;
      this.key = new KeySetting("key", defaultKey);
      this.key.attach(this);
   }

   public final String id() {
      return this.id;
   }

   public String name() {
      return I18n.translate("cloudvisuals.module." + this.id, new Object[0]);
   }

   public String description() {
      String key = "cloudvisuals.module." + this.id + ".description";
      return I18n.hasTranslation(key) ? I18n.translate(key, new Object[0]) : "";
   }

   public final Category category() {
      return this.category;
   }

   public final boolean enabledByDefault() {
      return this.enabledByDefault;
   }

   public final List<Setting<?>> settings() {
      return Collections.unmodifiableList(this.settings);
   }

   public final KeySetting key() {
      return this.key;
   }

   public final boolean isFavorite() {
      return this.favorite;
   }

   public final void setFavorite(boolean value) {
      if (this.favorite != value) {
         this.favorite = value;
         ConfigManager.markDirty();
      }
   }

   public void resetSettings() {
      for (Setting<?> setting : this.settings) {
         setting.reset();
      }

      this.key.reset();
      this.onConfigLoaded();
   }

   public boolean isAlwaysOn() {
      return false;
   }

   public boolean hasKeybind() {
      return !this.isAlwaysOn();
   }

   public final boolean isEnabled() {
      return this.isAlwaysOn() || this.enabled;
   }

   public final void setEnabled(boolean value) {
      if (!this.isAlwaysOn() && this.enabled != value) {
         this.enabled = value;
         if (value) {
            this.onEnable();
         } else {
            this.onDisable();
         }

         ConfigManager.markDirty();
      }
   }

   public final void toggle() {
      if (!this.isAlwaysOn()) {
         this.setEnabled(!this.enabled);
         Notifications.moduleToggled(this);
      }
   }

   public void onKeyBind() {
      this.toggle();
   }

   public void onRawKey(int key) {
   }

   public int icon() {
      return 57684;
   }

   protected final void section(String sectionId) {
      this.section = sectionId;
   }

   protected final void section(String sectionId, int icon) {
      this.section(sectionId);
      this.sectionIcons.put(sectionId, icon);
   }

   public final int sectionIcon(String sectionId) {
      return this.sectionIcons.getOrDefault(sectionId, 0);
   }

   protected final <S extends Setting<?>> S add(S setting) {
      setting.attach(this);
      setting.setSection(this.section);
      this.settings.add(setting);
      return setting;
   }

   protected void onRegistered() {
   }

   final void registered() {
      this.onRegistered();
   }

   protected void onEnable() {
   }

   protected void onDisable() {
   }

   public void onConfigLoaded() {
   }

   public void onTick() {
   }

   public void onHud(DrawContext g, RenderTickCounter deltaTracker) {
   }
}
