package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.cloudvisuals.module.Modules;
import net.minecraft.client.world.ClientWorld.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Properties.class)
public abstract class ClientLevelDataMixin {
   public ClientLevelDataMixin() {
   }

   @ModifyReturnValue(method = "getTimeOfDay", at = @At("RETURN"))
   private long cloudvisuals$dayTime(long time) {
      return Modules.timeWeather() != null ? Modules.timeWeather().dayTime(time) : time;
   }
}
