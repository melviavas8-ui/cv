package dev.cloudvisuals.mixin;

import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ProgressScreen.class)
public interface ProgressScreenAccessor {
   @Accessor("title")
   Text cloudvisuals$header();

   @Accessor("task")
   Text cloudvisuals$stage();

   @Accessor("progress")
   int cloudvisuals$progress();
}
