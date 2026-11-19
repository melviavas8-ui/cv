package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.cloudvisuals.gui.screen.ServerListDesign;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget$ServerEntry")
public abstract class OnlineServerEntryMixin {
   @Shadow
   @Final
   private MultiplayerScreen screen;
   @Shadow
   @Final
   private ServerInfo server;
   @Unique
   private int cloudvisuals$left;
   @Unique
   private int cloudvisuals$top;
   @Unique
   private int cloudvisuals$mouseX;
   @Unique
   private int cloudvisuals$mouseY;
   @Unique
   private boolean cloudvisuals$hovered;

   public OnlineServerEntryMixin() {
   }

   @Inject(method = "render", at = @At("HEAD"))
   private void cloudvisuals$remember(DrawContext g, int mouseX, int mouseY, boolean hovered, float partialTick, CallbackInfo ci) {
      Widget self = (Widget)(Object)this;
      this.cloudvisuals$left = self.getX() + 2;
      this.cloudvisuals$top = self.getY() + 2;
      this.cloudvisuals$mouseX = mouseX;
      this.cloudvisuals$mouseY = mouseY;
      this.cloudvisuals$hovered = hovered;
   }

   @WrapWithCondition(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"
      )
   )
   private boolean cloudvisuals$status(DrawContext g, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
      return width == 10 && height == 8 && ServerListDesign.active()
         ? !ServerListDesign.status(
            this,
            this.screen,
            this.server,
            g,
            x,
            y,
            this.cloudvisuals$left,
            this.cloudvisuals$top,
            this.cloudvisuals$hovered,
            this.cloudvisuals$mouseX,
            this.cloudvisuals$mouseY
         )
         : true;
   }

   @WrapOperation(
      method = "render",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Lnet/minecraft/text/StringVisitable;)I")
   )
   private int cloudvisuals$statusSpace(TextRenderer font, StringVisitable text, Operation<Integer> original) {
      int width = (Integer)original.call(new Object[]{font, text});
      return ServerListDesign.active() ? width + ServerListDesign.extraStatusWidth(this.server) : width;
   }

   @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$favorite(Click event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
      if (event.button() == 0 && ServerListDesign.click(this, this.screen, this.server, event.x(), event.y())) {
         cir.setReturnValue(true);
      }
   }
}
