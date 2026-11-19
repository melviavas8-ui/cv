package dev.cloudvisuals.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.item.TridentItem;

public final class Weapons {
   private Weapons() {
   }

   public static boolean is(ItemStack stack) {
      return stack.contains(DataComponentTypes.TOOL)
         || stack.contains(DataComponentTypes.WEAPON)
         || stack.getItem() instanceof TridentItem
         || stack.getItem() instanceof MaceItem;
   }
}
