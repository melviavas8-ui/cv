package dev.cloudvisuals.mixin;

import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.ItemLocksModule;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class LocalPlayerMixin {
   public LocalPlayerMixin() {
   }

   @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$lockedDrop(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
      ItemLocksModule locks = Modules.itemLocks();
      ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
      if (locks != null && locks.isLocked(player.getInventory().getSelectedSlot())) {
         locks.notifyBlocked();
         cir.setReturnValue(false);
      }
   }
}
