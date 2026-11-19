package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.cloudvisuals.CloudVisualsClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public abstract class MinecraftTitleMixin {
   public MinecraftTitleMixin() {
   }

   @ModifyReturnValue(method = "getWindowTitle", at = @At("RETURN"))
   private String cloudvisuals$testTitle(String title) {
      return CloudVisualsClient.uiTest() == null
         ? title
         : title + " \u2014 \u0410\u0412\u0422\u041e\u0422\u0415\u0421\u0422 Cloud Visuals, \u043d\u0435 \u0442\u0440\u043e\u0433\u0430\u0442\u044c";
   }
}
