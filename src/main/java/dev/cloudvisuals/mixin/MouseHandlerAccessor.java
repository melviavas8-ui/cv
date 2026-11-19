package dev.cloudvisuals.mixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mouse.class)
public interface MouseHandlerAccessor {
   @Accessor("x")
   void cloudvisuals$setX(double var1);

   @Accessor("y")
   void cloudvisuals$setY(double var1);
}
