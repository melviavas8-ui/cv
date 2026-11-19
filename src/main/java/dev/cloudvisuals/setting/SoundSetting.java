package dev.cloudvisuals.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.cloudvisuals.sound.SoundLibrary;
import java.util.List;
import net.minecraft.client.resource.language.I18n;

public class SoundSetting extends Setting<String> {
   public SoundSetting(String id, String defaultSound) {
      super(id, defaultSound);
   }

   public List<String> options() {
      return SoundLibrary.options();
   }

   public static String label(String sound) {
      if (sound.startsWith("file:")) {
         return sound.substring(5);
      }

      String key = "cloudvisuals.sound." + sound;
      return I18n.hasTranslation(key) ? I18n.translate(key, new Object[0]) : sound;
   }

   public void preview(float volume) {
      SoundLibrary.play(this.get(), volume, 1.0F);
   }

   @Override
   public JsonElement toJson() {
      return new JsonPrimitive(this.get());
   }

   @Override
   public void fromJson(JsonElement json) {
      if (json != null && json.isJsonPrimitive()) {
         this.set(json.getAsString());
      }
   }
}
