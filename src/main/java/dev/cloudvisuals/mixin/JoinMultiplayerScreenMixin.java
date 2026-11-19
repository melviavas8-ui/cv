package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.cloudvisuals.gui.screen.ServerListDesign;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin {
   public JoinMultiplayerScreenMixin() {
   }

   @ModifyArg(
      method = "init",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;<init>(Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;Lnet/minecraft/client/MinecraftClient;IIII)V"
      ),
      index = 5
   )
   private int cloudvisuals$rowHeight(int height) {
      return ServerListDesign.active() ? 42 : height;
   }

   @WrapOperation(
      method = "init",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/widget/ThreePartsLayoutWidget;addHeader(Lnet/minecraft/text/Text;Lnet/minecraft/client/font/TextRenderer;)V"
      )
   )
   private void cloudvisuals$title(ThreePartsLayoutWidget layout, Text title, TextRenderer font, Operation<Void> original) {
      original.call(new Object[]{layout, ServerListDesign.active() ? ScreenTexts.EMPTY : title, font});
   }
}
