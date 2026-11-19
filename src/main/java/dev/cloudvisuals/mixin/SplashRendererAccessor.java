package dev.cloudvisuals.mixin;

import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SplashTextRenderer.class)
public interface SplashRendererAccessor {
   @Accessor("text")
   Text cloudvisuals$text();
}
