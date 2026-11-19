package dev.cloudvisuals.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil.Type;

public class KeySetting extends Setting<Integer> {
   public static final int NONE = -1;

   public KeySetting(String id, int defaultKey) {
      super(id, defaultKey);
   }

   public boolean isBound() {
      return this.get() != -1;
   }

   public String keyName() {
      return !this.isBound() ? I18n.translate("cloudvisuals.key.none", new Object[0]) : Type.KEYSYM.createFromCode(this.get()).getLocalizedText().getString();
   }

   @Override
   public JsonElement toJson() {
      return new JsonPrimitive(this.get());
   }

   @Override
   public void fromJson(JsonElement json) {
      if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
         this.set(json.getAsInt());
      }
   }
}
