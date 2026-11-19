package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.cloudvisuals.gui.screen.ServerListDesign;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScrollableWidget.class)
public abstract class AbstractScrollAreaMixin {
   public AbstractScrollAreaMixin() {
   }

   @WrapWithCondition(
      method = "drawScrollbar",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"
      )
   )
   private boolean cloudvisuals$scrollbar(DrawContext g, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
      return !((Object)this instanceof EntryListWidget<?> list && ServerListDesign.styled(list) && ServerListDesign.scrollbar(g, sprite, x, y, width, height));
   }
}
