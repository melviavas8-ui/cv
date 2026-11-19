package dev.cloudvisuals.mixin;

import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.hud.GlassBossbarModule;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public abstract class BossHealthOverlayMixin {
   @Shadow
   @Final
   Map<UUID, ClientBossBar> bossBars;
   @Unique
   private float cloudvisuals$shift;

   public BossHealthOverlayMixin() {
   }

   @Inject(method = "render", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$glassBossbar(DrawContext guiGraphics, CallbackInfo ci) {
      GlassBossbarModule bossbar = Modules.glassBossbar();
      if (bossbar != null && bossbar.isEnabled()) {
         bossbar.render(guiGraphics, this.bossBars.values());
         ci.cancel();
      } else {
         this.cloudvisuals$shift = Modules.island() != null && !this.bossBars.isEmpty()
            ? Math.max(0.0F, Modules.island().reservedTop(guiGraphics.getScaledWindowWidth()) - 7.0F)
            : 0.0F;
         if (this.cloudvisuals$shift > 0.0F) {
            guiGraphics.getMatrices().pushMatrix();
            guiGraphics.getMatrices().translate(0.0F, this.cloudvisuals$shift);
         }
      }
   }

   @Inject(method = "render", at = @At("RETURN"))
   private void cloudvisuals$restoreBossbar(DrawContext guiGraphics, CallbackInfo ci) {
      if (this.cloudvisuals$shift > 0.0F) {
         guiGraphics.getMatrices().popMatrix();
         this.cloudvisuals$shift = 0.0F;
      }
   }
}
