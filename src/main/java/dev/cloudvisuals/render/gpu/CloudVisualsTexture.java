package dev.cloudvisuals.render.gpu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

public class CloudVisualsTexture extends NativeImageBackedTexture {
   public CloudVisualsTexture(String label, NativeImage image) {
      super(() -> "Cloud Visuals " + label, image);
   }

   public CloudVisualsTexture(String label, int width, int height) {
      super(() -> "Cloud Visuals " + label, width, height, true);
   }

   public CloudVisualsTexture filter(boolean linear, boolean repeat) {
      FilterMode mode = linear ? FilterMode.LINEAR : FilterMode.NEAREST;
      AddressMode address = repeat ? AddressMode.REPEAT : AddressMode.CLAMP_TO_EDGE;
      this.sampler = RenderSystem.getSamplerCache().get(address, address, mode, mode, false);
      return this;
   }

   public NativeImage image() {
      NativeImage pixels = this.getImage();
      if (pixels == null) {
         throw new IllegalStateException("Texture is closed");
      } else {
         return pixels;
      }
   }
}
