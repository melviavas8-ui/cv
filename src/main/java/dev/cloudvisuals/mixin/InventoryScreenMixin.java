package dev.cloudvisuals.mixin;

import dev.cloudvisuals.gui.screen.InventoryDesign;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
   public InventoryScreenMixin() {
   }

   @ModifyArg(
      method = "drawForeground",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V"
      ),
      index = 4
   )
   private int cloudvisuals$labelColor(int color) {
      return InventoryDesign.handles((HandledScreen<?>)(Object)this) ? -1644564 : color;
   }
}
