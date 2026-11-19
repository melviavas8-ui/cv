package dev.cloudvisuals.module.impl.visual;

import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import dev.cloudvisuals.render.CloudVisualsRenderTypes;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.render.world.Geometry;
import dev.cloudvisuals.setting.BoolSetting;
import dev.cloudvisuals.setting.NumberSetting;
import dev.cloudvisuals.util.ColorUtil;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class TrajectoriesModule extends Module {
   private static final int MAX_STEPS = 320;
   private final NumberSetting width = this.add(new NumberSetting("width", 2.0, 1.0, 5.0, 0.5, "cloudvisuals.unit.pixels"));
   private final BoolSetting landing = this.add(new BoolSetting("landing", true));

   public TrajectoriesModule() {
      super("trajectories", Category.VISUALS, false);
   }

   @Override
   public int icon() {
      return 58251;
   }

   private static TrajectoriesModule.Throw throwFor(ClientPlayerEntity player, Hand hand) {
      ItemStack stack = player.getStackInHand(hand);
      boolean using = player.isUsingItem() && player.getActiveHand() == hand;
      if (stack.isOf(Items.BOW)) {
         if (!using) {
            return null;
         }

         float power = BowItem.getPullProgress(stack.getMaxUseTime(player) - player.getItemUseTimeLeft());
         return power < 0.1F ? null : new TrajectoriesModule.Throw(power * 3.0F, 0.05, 0.99, 0.0F);
      } else if (stack.isOf(Items.CROSSBOW)) {
         if (!CrossbowItem.isCharged(stack)) {
            return null;
         }

         ChargedProjectilesComponent charged = (ChargedProjectilesComponent)stack.get(DataComponentTypes.CHARGED_PROJECTILES);
         boolean firework = charged != null && charged.contains(Items.FIREWORK_ROCKET);
         return firework ? null : new TrajectoriesModule.Throw(3.15F, 0.05, 0.99, 0.0F);
      } else if (stack.isOf(Items.TRIDENT)) {
         return using ? new TrajectoriesModule.Throw(2.5F, 0.05, 0.99, 0.0F) : null;
      } else if (stack.isOf(Items.ENDER_PEARL) || stack.isOf(Items.SNOWBALL) || stack.isOf(Items.EGG)) {
         return new TrajectoriesModule.Throw(1.5F, 0.03, 0.99, 0.0F);
      } else if (!stack.isOf(Items.SPLASH_POTION) && !stack.isOf(Items.LINGERING_POTION)) {
         return stack.isOf(Items.EXPERIENCE_BOTTLE) ? new TrajectoriesModule.Throw(0.7F, 0.07, 0.99, -20.0F) : null;
      } else {
         return new TrajectoriesModule.Throw(0.5F, 0.05, 0.99, -20.0F);
      }
   }

   public void render(WorldRenderContext context) {
      MinecraftClient mc = MinecraftClient.getInstance();
      ClientPlayerEntity player = mc.player;
      MatrixStack pose = context.matrices();
      if (this.isEnabled() && player != null && mc.world != null && pose != null && mc.options.getPerspective().isFirstPerson()) {
         Hand hand = Hand.MAIN_HAND;
         TrajectoriesModule.Throw shot = throwFor(player, hand);
         if (shot == null) {
            hand = Hand.OFF_HAND;
            shot = throwFor(player, hand);
         }

         if (shot != null) {
            float partial = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false);
            float yaw = player.getYaw(partial);
            float pitch = player.getPitch(partial);
            double x = MathHelper.lerp(partial, player.lastX, player.getX());
            double y = MathHelper.lerp(partial, player.lastY, player.getY()) + player.getStandingEyeHeight() - 0.1;
            double z = MathHelper.lerp(partial, player.lastZ, player.getZ());
            float rad = (float) (Math.PI / 180.0);
            Vec3d direction = new Vec3d(
                  -MathHelper.sin(yaw * rad) * MathHelper.cos(pitch * rad),
                  -MathHelper.sin((pitch + shot.pitchOffset()) * rad),
                  MathHelper.cos(yaw * rad) * MathHelper.cos(pitch * rad)
               )
               .normalize()
               .multiply(shot.speed());
            Vec3d motion = player.getVelocity();
            Vec3d velocity = direction.add(motion.x, player.isOnGround() ? 0.0 : motion.y, motion.z);
            List<Vec3d> points = new ArrayList<>();
            Vec3d position = new Vec3d(x, y, z);
            points.add(position);
            BlockHitResult hit = null;

            for (int step = 0; step < 320; step++) {
               Vec3d next = position.add(velocity);
               BlockHitResult clip = mc.world.raycast(new RaycastContext(position, next, ShapeType.COLLIDER, FluidHandling.NONE, player));
               if (clip.getType() != Type.MISS) {
                  points.add(clip.getPos());
                  hit = clip;
                  break;
               }

               position = next;
               points.add(position);
               boolean water = mc.world.getFluidState(BlockPos.ofFloored(position)).isStill();
               velocity = velocity.multiply(water ? 0.6 : shot.drag()).subtract(0.0, shot.gravity(), 0.0);
               if (position.y < mc.world.getBottomY() - 32) {
                  break;
               }
            }

            Camera camera = context.gameRenderer().getCamera();
            Vec3d cam = camera.getCameraPos();
            Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            boolean rightHand = arm == Arm.RIGHT;
            Vec3d side = new Vec3d(camera.getDiagonalPlane()).multiply(rightHand ? -0.28 : 0.28).add(new Vec3d(camera.getVerticalPlane()).multiply(-0.16));
            Immediate buffers = mc.getBufferBuilders().getEntityVertexConsumers();
            int from = Theme.accent();
            int to = Theme.accent2();
            Vec3d start = points.get(0);
            int count = points.size();

            for (int pass = 0; pass < 2; pass++) {
               RenderLayer type = CloudVisualsRenderTypes.lines();
               VertexConsumer lines = Geometry.lines(buffers.getBuffer(type), pass == 0 ? this.width.floatValue() * 2.6F : this.width.floatValue());
               Entry last = pose.peek();
               float alpha = pass == 0 ? 0.22F : 0.95F;

               for (int i = 1; i < count; i++) {
                  Vec3d a = points.get(i - 1);
                  Vec3d b = points.get(i);
                  double distance = b.distanceTo(start);
                  if (!(distance < 0.8)) {
                     float t = (float)i / Math.max(1, count - 1);
                     float fade = (float)Math.min(1.0, (distance - 0.8) / 1.6);
                     Vec3d offsetA = side.multiply(Math.max(0.0, 1.0 - a.distanceTo(start) / 7.0));
                     Vec3d offsetB = side.multiply(Math.max(0.0, 1.0 - distance / 7.0));
                     line(lines, last, a.add(offsetA).subtract(cam), b.add(offsetB).subtract(cam), ColorUtil.lerp(from, to, t), alpha * fade);
                  }
               }

               if (this.landing.isOn() && hit != null) {
                  double distance = hit.getPos().distanceTo(start);
                  ring(lines, last, hit.getPos().subtract(cam), hit.getSide(), to, (float)(0.28 + distance * 0.012), alpha);
               }

               buffers.draw(type);
            }
         }
      }
   }

   private static void line(VertexConsumer lines, Entry pose, Vec3d a, Vec3d b, int color, float alpha) {
      float dx = (float)(b.x - a.x);
      float dy = (float)(b.y - a.y);
      float dz = (float)(b.z - a.z);
      float length = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
      if (!(length < 1.0E-4F)) {
         dx /= length;
         dy /= length;
         dz /= length;
         float r = ColorUtil.red(color) / 255.0F;
         float g = ColorUtil.green(color) / 255.0F;
         float bl = ColorUtil.blue(color) / 255.0F;
         lines.vertex(pose, (float)a.x, (float)a.y, (float)a.z).color(r, g, bl, alpha).normal(pose, dx, dy, dz);
         lines.vertex(pose, (float)b.x, (float)b.y, (float)b.z).color(r, g, bl, alpha).normal(pose, dx, dy, dz);
      }
   }

   private static void ring(VertexConsumer lines, Entry pose, Vec3d center, Direction face, int color, float radius, float alpha) {
      Vec3d normal = Vec3d.of(face.getVector());
      Vec3d axisA = Math.abs(normal.y) > 0.5 ? new Vec3d(1.0, 0.0, 0.0) : new Vec3d(0.0, 1.0, 0.0);
      Vec3d axisB = normal.crossProduct(axisA).normalize();
      axisA = axisB.crossProduct(normal).normalize();
      Vec3d base = center.add(normal.multiply(0.02));
      float pulse = radius * (1.0F + 0.12F * MathHelper.sin((float)System.currentTimeMillis() / 180.0F));
      int segments = 28;

      for (int i = 0; i < segments; i++) {
         double a0 = (Math.PI * 2) * i / segments;
         double a1 = (Math.PI * 2) * (i + 1) / segments;
         Vec3d p0 = base.add(axisA.multiply(Math.cos(a0) * pulse)).add(axisB.multiply(Math.sin(a0) * pulse));
         Vec3d p1 = base.add(axisA.multiply(Math.cos(a1) * pulse)).add(axisB.multiply(Math.sin(a1) * pulse));
         line(lines, pose, p0, p1, color, alpha);
      }
   }

   private record Throw(float speed, double gravity, double drag, float pitchOffset) {
      private Throw {
      }
   }
}
