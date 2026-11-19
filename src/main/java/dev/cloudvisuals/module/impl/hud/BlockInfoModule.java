package dev.cloudvisuals.module.impl.hud;

import dev.cloudvisuals.hud.Anchor;
import dev.cloudvisuals.hud.HudContext;
import dev.cloudvisuals.hud.HudElement;
import dev.cloudvisuals.hud.Size;
import dev.cloudvisuals.render.Theme;
import dev.cloudvisuals.setting.BoolSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;

public final class BlockInfoModule extends HudElement {
   private final BoolSetting tool = this.add(new BoolSetting("tool", true));

   public BlockInfoModule() {
      super("block_info", false, Anchor.TOP, 0.0F, 6.0F);
   }

   @Override
   public int icon() {
      return 58679;
   }

   private BlockInfoModule.Info info(HudContext hud) {
      if (hud.preview) {
         return new BlockInfoModule.Info(
            new ItemStack(Items.DEEPSLATE_DIAMOND_ORE),
            I18n.translate("block.minecraft.deepslate_diamond_ore", new Object[0]),
            58822,
            I18n.translate("cloudvisuals.hud.block_info.pickaxe", new Object[0]),
            true
         );
      }

      if (hud.mc.world != null && hud.mc.player != null && hud.mc.crosshairTarget instanceof BlockHitResult hit && hit.getType() == Type.BLOCK) {
         BlockPos pos = hit.getBlockPos();
         BlockState state = hud.mc.world.getBlockState(pos);
         if (state.isAir()) {
            return null;
         }

         ItemStack icon = state.getPickStack(hud.mc.world, pos, false);
         int toolIcon = 0;
         String toolName = "";
         if (state.isIn(BlockTags.PICKAXE_MINEABLE)) {
            toolIcon = 58822;
            toolName = I18n.translate("cloudvisuals.hud.block_info.pickaxe", new Object[0]);
         } else if (state.isIn(BlockTags.AXE_MINEABLE)) {
            toolIcon = 57424;
            toolName = I18n.translate("cloudvisuals.hud.block_info.axe", new Object[0]);
         } else if (state.isIn(BlockTags.SHOVEL_MINEABLE)) {
            toolIcon = 57693;
            toolName = I18n.translate("cloudvisuals.hud.block_info.shovel", new Object[0]);
         } else if (state.isIn(BlockTags.HOE_MINEABLE)) {
            toolIcon = 57835;
            toolName = I18n.translate("cloudvisuals.hud.block_info.hoe", new Object[0]);
         }

         boolean correct = !state.isToolRequired() || hud.mc.player.getMainHandStack().isSuitableFor(state);
         return new BlockInfoModule.Info(icon, state.getBlock().getName().getString(), toolIcon, toolName, correct);
      } else {
         return null;
      }
   }

   @Override
   protected boolean hasContent(HudContext hud) {
      return this.info(hud) != null;
   }

   @Override
   protected float defaultRoundness() {
      return 0.6F;
   }

   @Override
   protected Size measure(HudContext hud) {
      BlockInfoModule.Info info = this.info(hud);
      if (info == null) {
         return new Size(60.0F, 22.0F);
      }

      float text = hud.semibold.width(info.name(), 7.4F);
      if (this.tool.isOn() && !info.toolName().isEmpty()) {
         text = Math.max(text, 9.0F + hud.regular.width(info.toolName(), 6.2F));
      }

      boolean twoLines = this.tool.isOn() && !info.toolName().isEmpty();
      return new Size(29.0F + text + 9.0F, twoLines ? 28.0F : 22.0F);
   }

   @Override
   protected void render(HudContext hud, float w, float h) {
      BlockInfoModule.Info info = this.info(hud);
      if (info != null) {
         hud.item(info.icon(), 7.0F, (h - 16.0F) / 2.0F, 16.0F);
         float textX = 29.0F;
         boolean twoLines = this.tool.isOn() && !info.toolName().isEmpty();
         hud.text(hud.semibold, info.name(), textX, twoLines ? 5.5F : HudContext.centered(0.0F, h, 7.4F), 7.4F, hud.label);
         if (twoLines) {
            int color = info.correct() ? Theme.positive : Theme.danger;
            hud.icon(info.toolIcon(), textX + 3.5F, 19.5F, 7.0F, color);
            hud.text(hud.regular, info.toolName(), textX + 9.0F, 16.0F, 6.2F, hud.secondary);
         }
      }
   }

   private record Info(ItemStack icon, String name, int toolIcon, String toolName, boolean correct) {
      private Info {
      }
   }
}
