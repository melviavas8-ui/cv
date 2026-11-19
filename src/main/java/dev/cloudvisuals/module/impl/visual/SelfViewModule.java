package dev.cloudvisuals.module.impl.visual;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;

public final class SelfViewModule extends Module {
   private final BoolSetting arms = this.add(new BoolSetting("arms", false));
   private final BoolSetting cape = this.add(new BoolSetting("cape", true));
   private final BoolSetting whileEating = this.add(new BoolSetting("while_eating", true));

   public SelfViewModule() {
      super("self_view", Category.VISUALS, false);
   }

   @Override
   public int icon() {
      return 58472;
   }

   public boolean showsSelf() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (this.isEnabled() && mc.player != null && mc.options.getPerspective().isFirstPerson()) {
         return !mc.player.isSleeping() && !mc.player.isUsingSpyglass() ? this.whileEating.isOn() || !mc.player.isUsingItem() : false;
      } else {
         return false;
      }
   }

   public void hideParts(PlayerEntityModel model) {
      model.head.visible = false;
      model.hat.visible = false;
      if (!this.arms.isOn()) {
         model.leftArm.visible = false;
         model.rightArm.visible = false;
         model.leftSleeve.visible = false;
         model.rightSleeve.visible = false;
      }
   }

   public boolean showsCape() {
      return this.cape.isOn();
   }

   public boolean isSelf(Object entity) {
      return entity instanceof ClientPlayerEntity && this.showsSelf();
   }

   public boolean isSelf(PlayerEntityRenderState state) {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      return player != null && state.id == player.getId() && this.showsSelf();
   }
}
