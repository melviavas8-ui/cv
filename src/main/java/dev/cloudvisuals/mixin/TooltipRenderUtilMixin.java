package dev.cloudvisuals.mixin;

import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.visual.GlassTooltipsModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TooltipBackgroundRenderer.class)
public abstract class TooltipRenderUtilMixin {
   public TooltipRenderUtilMixin() {
   }

   @Inject(method = "render", at = @At("HEAD"), cancellable = true)
   private static void cloudvisuals$glassTooltip(DrawContext g, int x, int y, int width, int height, @Nullable Identifier style, CallbackInfo ci) {
      GlassTooltipsModule module = Modules.glassTooltips();
      if (module != null && module.isEnabled() && style == null) {
         module.renderBackground(g, x, y, width, height, 0);
         ci.cancel();
      }
   }
}
