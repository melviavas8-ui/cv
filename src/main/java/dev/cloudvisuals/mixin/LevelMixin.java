package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.cloudvisuals.module.Modules;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(World.class)
public abstract class LevelMixin {
   public LevelMixin() {
   }

   @ModifyReturnValue(method = "getRainGradient", at = @At("RETURN"))
   private float cloudvisuals$rain(float rain) {
      return (Object)this instanceof ClientWorld && Modules.timeWeather() != null ? Modules.timeWeather().rainLevel(rain) : rain;
   }

   @ModifyReturnValue(method = "getThunderGradient", at = @At("RETURN"))
   private float cloudvisuals$thunder(float thunder) {
      return (Object)this instanceof ClientWorld && Modules.timeWeather() != null ? Modules.timeWeather().thunderLevel(thunder) : thunder;
   }
}
