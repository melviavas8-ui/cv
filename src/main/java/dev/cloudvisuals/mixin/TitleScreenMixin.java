package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.cloudvisuals.gui.screen.TitleDesign;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
   @Shadow
   @Nullable
   private SplashTextRenderer splashText;
   @Shadow
   private boolean doBackgroundFade;
   @Shadow
   private long backgroundFadeStart;

   protected TitleScreenMixin(Text title) {
      super(title);
   }

   @Inject(method = "render", at = @At("HEAD"))
   private void cloudvisuals$resetFade(DrawContext g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      TitleDesign.setFade(0.0F);
   }

   @Inject(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/screen/TitleScreen;renderPanoramaBackground(Lnet/minecraft/client/gui/DrawContext;F)V",
         shift = Shift.AFTER
      )
   )
   private void cloudvisuals$backdrop(DrawContext g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      TitleDesign.renderBackdrop(g, this.width, this.height);
   }

   @WrapWithCondition(
      method = "render",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LogoDrawer;draw(Lnet/minecraft/client/gui/DrawContext;IF)V")
   )
   private boolean cloudvisuals$logo(LogoDrawer logo, DrawContext g, int screenWidth, float alpha) {
      TitleDesign.setFade(alpha);
      return !TitleDesign.laidOut();
   }

   @WrapWithCondition(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/screen/SplashTextRenderer;render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/client/font/TextRenderer;F)V"
      )
   )
   private boolean cloudvisuals$splash(SplashTextRenderer renderer, DrawContext g, int screenWidth, TextRenderer font, float alpha) {
      return !TitleDesign.laidOut();
   }

   @WrapWithCondition(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V"
      )
   )
   private boolean cloudvisuals$version(DrawContext g, TextRenderer font, String text, int x, int y, int color) {
      return !TitleDesign.laidOut();
   }

   @WrapOperation(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/realms/gui/screen/RealmsNotificationsScreen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V"
      )
   )
   private void cloudvisuals$realmsIcons(RealmsNotificationsScreen screen, DrawContext g, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
      if (!TitleDesign.laidOut()) {
         original.call(new Object[]{screen, g, mouseX, mouseY, partialTick});
      } else {
         g.getMatrices().pushMatrix();
         g.getMatrices().translate(TitleDesign.realmsDx(), TitleDesign.realmsDy());
         original.call(new Object[]{screen, g, mouseX - TitleDesign.realmsDx(), mouseY - TitleDesign.realmsDy(), partialTick});
         g.getMatrices().popMatrix();
      }
   }

   @Inject(method = "render", at = @At("TAIL"))
   private void cloudvisuals$foreground(DrawContext g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      TitleDesign.renderForeground(
         g, this.width, this.height, mouseX, mouseY, this.splashText == null ? null : ((SplashRendererAccessor)this.splashText).cloudvisuals$text().getString()
      );
   }

   @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$click(Click event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
      if (event.button() == 0 && TitleDesign.mouseClicked(event.x(), event.y())) {
         cir.setReturnValue(true);
      }
   }
}
