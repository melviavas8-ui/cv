package dev.cloudvisuals.mixin;

import dev.cloudvisuals.gui.screen.StatusDesign;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin {
   public ConnectScreenMixin() {
   }

   @Inject(
      method = "connect(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;ZLnet/minecraft/client/network/CookieStorage;)V",
      at = @At("HEAD")
   )
   private static void cloudvisuals$rememberServer(
      Screen parent, MinecraftClient minecraft, ServerAddress address, ServerInfo server, boolean quickPlay, CookieStorage transferState, CallbackInfo ci
   ) {
      StatusDesign.connecting(server);
   }
}
