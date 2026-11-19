package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.cosmetic.CapeModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.Identifier;
import net.minecraft.util.AssetInfo.TextureAssetInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerMixin {
   public AbstractClientPlayerMixin() {
   }

   @ModifyReturnValue(method = "getSkin", at = @At("RETURN"))
   private SkinTextures cloudvisuals$ownCape(SkinTextures skin) {
      if ((Object)this != MinecraftClient.getInstance().player) {
         return skin;
      }

      CapeModule cape = Modules.cape();
      Identifier texture = cape != null ? cape.texture() : null;
      return texture == null ? skin : new SkinTextures(skin.body(), new TextureAssetInfo(texture, texture), skin.elytra(), skin.model(), skin.secure());
   }
}
