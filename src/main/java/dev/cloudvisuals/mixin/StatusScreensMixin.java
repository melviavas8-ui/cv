package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.cloudvisuals.gui.screen.StatusDesign;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({ConnectScreen.class, LevelLoadingScreen.class, ProgressScreen.class})
public abstract class StatusScreensMixin {
   public StatusScreensMixin() {
   }

   @WrapWithCondition(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawCenteredTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"
      )
   )
   private boolean cloudvisuals$text(DrawContext g, TextRenderer font, Text text, int x, int y, int color) {
      return !StatusDesign.styled((Screen)(Object)this);
   }
}
