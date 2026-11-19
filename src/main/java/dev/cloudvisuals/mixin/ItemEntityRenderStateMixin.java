package dev.cloudvisuals.mixin;

import dev.cloudvisuals.module.impl.world.ItemPhysicsModule;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntityRenderState.class)
public abstract class ItemEntityRenderStateMixin implements ItemPhysicsModule.State {
   @Unique
   private boolean cloudvisuals$onGround;
   @Unique
   private int cloudvisuals$id;
   @Unique
   private boolean cloudvisuals$block;

   public ItemEntityRenderStateMixin() {
   }

   @Override
   public void cloudvisuals$physics(boolean onGround, int id, boolean block) {
      this.cloudvisuals$onGround = onGround;
      this.cloudvisuals$id = id;
      this.cloudvisuals$block = block;
   }

   @Override
   public boolean cloudvisuals$onGround() {
      return this.cloudvisuals$onGround;
   }

   @Override
   public int cloudvisuals$id() {
      return this.cloudvisuals$id;
   }

   @Override
   public boolean cloudvisuals$block() {
      return this.cloudvisuals$block;
   }
}
