package dev.cloudvisuals.mixin;

import dev.cloudvisuals.gui.screen.ServerListDesign;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.Widget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntryListWidget.class)
public abstract class AbstractSelectionListMixin {
   public AbstractSelectionListMixin() {
   }

   @Inject(method = "drawMenuListBackground", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$background(DrawContext g, CallbackInfo ci) {
      if (ServerListDesign.styled(this)) {
         ci.cancel();
      }
   }

   @Inject(method = "drawHeaderAndFooterSeparators", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$separators(DrawContext g, CallbackInfo ci) {
      if (ServerListDesign.styled(this)) {
         ci.cancel();
      }
   }

   @Inject(method = "renderEntry", at = @At("HEAD"))
   private void cloudvisuals$card(DrawContext g, int mouseX, int mouseY, float partialTick, @Coerce Widget entry, CallbackInfo ci) {
      if (ServerListDesign.styled(this)) {
         EntryListWidget<?> list = (EntryListWidget<?>)(Object)this;
         int index = list.children().indexOf(entry);
         if (index >= 0) {
            ServerListDesign.card(
               list,
               g,
               index,
               entry.getX(),
               entry.getY(),
               entry.getWidth(),
               entry.getHeight(),
               list.getSelectedOrNull() == entry,
               list.isMouseOver(mouseX, mouseY) && ((Element)entry).isMouseOver(mouseX, mouseY)
            );
         }
      }
   }

   @Inject(method = "drawSelectionHighlight", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$selection(DrawContext g, @Coerce Widget entry, int color, CallbackInfo ci) {
      if (ServerListDesign.styled(this)) {
         ci.cancel();
      }
   }
}
