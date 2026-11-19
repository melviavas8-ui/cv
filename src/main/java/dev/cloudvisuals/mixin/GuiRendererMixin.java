package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;
import dev.cloudvisuals.render.gpu.GuiDraw;
import dev.cloudvisuals.render.gpu.ItemAlpha;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
   @Shadow
   @Final
   private List<?> preparations;

   public GuiRendererMixin() {
   }

   @Inject(method = "endBuffer", at = @At("HEAD"))
   private void cloudvisuals$rememberMarkers(
      BufferBuilder builder, RenderPipeline pipeline, TextureSetup textureSetup, @Nullable ScreenRect scissor, CallbackInfo ci
   ) {
      GuiDraw.onRecordMesh(pipeline, this.preparations.size());
   }

   @WrapOperation(
      method = "renderPreparedDraws",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Ljava/util/function/Supplier;Lnet/minecraft/client/gl/Framebuffer;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;II)V"
      )
   )
   private void cloudvisuals$drawAroundMarkers(
      GuiRenderer self,
      Supplier<String> label,
      Framebuffer target,
      GpuBufferSlice fog,
      GpuBufferSlice transforms,
      GpuBuffer indices,
      IndexType indexType,
      int from,
      int to,
      Operation<Void> original
   ) {
      if (!GuiDraw.hasRuns()) {
         original.call(new Object[]{self, label, target, fog, transforms, indices, indexType, from, to});
      } else {
         int start = from;

         for (int i = from; i < to; i++) {
            List<GuiDraw.Element> run = GuiDraw.runAt(i);
            if (run != null) {
               if (start < i) {
                  original.call(new Object[]{self, label, target, fog, transforms, indices, indexType, start, i});
               }

               GuiDraw.execute(run, target);
               start = i + 1;
            }
         }

         if (start < to) {
            original.call(new Object[]{self, label, target, fog, transforms, indices, indexType, start, to});
         }
      }
   }

   @ModifyArg(
      method = "prepareItem",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/render/state/TexturedQuadGuiElementRenderState;<init>(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/texture/TextureSetup;Lorg/joml/Matrix3x2f;IIIIFFFFILnet/minecraft/client/gui/ScreenRect;Lnet/minecraft/client/gui/ScreenRect;)V"
      ),
      index = 11
   )
   private int cloudvisuals$itemAlpha(int color, @Local(argsOnly = true) ItemGuiElementRenderState state) {
      float alpha = ((ItemAlpha.Holder)(Object)state).cloudvisuals$alpha();
      return alpha >= 0.999F ? color : ItemAlpha.blitColor(alpha);
   }

   @Inject(method = "render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", at = @At("TAIL"))
   private void cloudvisuals$frameDrawn(GpuBufferSlice fog, CallbackInfo ci) {
      GuiDraw.onGuiRendered();
   }
}
