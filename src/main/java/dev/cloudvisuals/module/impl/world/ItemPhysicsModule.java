package dev.cloudvisuals.module.impl.world;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.setting.NumberSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public final class ItemPhysicsModule extends Module {
   private final NumberSetting spin = this.add(new NumberSetting("spin", 100.0, 0.0, 200.0, 10.0, "cloudvisuals.unit.percent"));

   public ItemPhysicsModule() {
      super("item_physics", Category.WORLD, false);
   }

   @Override
   public int icon() {
      return 58090;
   }

   public float lift(ItemPhysicsModule.State item, float rest) {
      return item.cloudvisuals$onGround() && !item.cloudvisuals$block() ? 0.02F : rest;
   }

   public void rotate(MatrixStack pose, ItemPhysicsModule.State item, float ageInTicks) {
      boolean block = item.cloudvisuals$block();
      float yaw = item.cloudvisuals$id() * 37 % 360;
      if (item.cloudvisuals$onGround()) {
         pose.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
         if (!block) {
            pose.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
         }
      } else {
         float age = ageInTicks * this.spin.floatValue() / 100.0F;
         pose.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw + age * 9.0F));
         pose.multiply(RotationAxis.POSITIVE_X.rotationDegrees(age * 14.0F));
      }
   }

   public interface State {
      void cloudvisuals$physics(boolean var1, int var2, boolean var3);

      boolean cloudvisuals$onGround();

      int cloudvisuals$id();

      boolean cloudvisuals$block();
   }
}
