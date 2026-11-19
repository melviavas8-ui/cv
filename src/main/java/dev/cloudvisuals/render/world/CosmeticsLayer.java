package dev.cloudvisuals.render.world;

import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.cosmetic.HatsModule;
import dev.cloudvisuals.module.impl.cosmetic.WingsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

public final class CosmeticsLayer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
   private final RecordingBuffers buffers = new RecordingBuffers();

   public CosmeticsLayer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> parent) {
      super(parent);
   }

   @Override
   public void render(MatrixStack pose, OrderedRenderCommandQueue collector, int light, PlayerEntityRenderState state, float yRot, float xRot) {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null && state.id == player.getId() && !state.invisible) {
         float age = state.age;
         HatsModule hats = Modules.hats();
         if (hats != null && hats.isEnabled()) {
            pose.push();

            try {
               ((PlayerEntityModel)this.getContextModel()).head.applyTransform(pose);
               hats.render(pose, this.buffers, light, age);
            } catch (RuntimeException e) {
               Modules.fail(hats, e);
            } finally {
               pose.pop();
            }
         }

         WingsModule wings = Modules.wings();
         if (wings != null && wings.isEnabled() && !state.isGliding) {
            pose.push();

            try {
               ((PlayerEntityModel)this.getContextModel()).body.applyTransform(pose);
               wings.render(pose, this.buffers, light, player, age);
            } catch (RuntimeException e) {
               Modules.fail(wings, e);
            } finally {
               pose.pop();
            }
         }

         this.buffers.submit(collector);
      }
   }
}
