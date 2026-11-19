package dev.cloudvisuals.module.impl;

import dev.cloudvisuals.gui.screen.GlassSearchField;
import dev.cloudvisuals.module.Category;
import dev.cloudvisuals.module.Module;
import java.util.Locale;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

public final class ContainerSearchModule extends Module {
   private GlassSearchField box;
   private Screen owner;

   public ContainerSearchModule() {
      super("container_search", Category.INTERFACE, true);
   }

   @Override
   public int icon() {
      return 57681;
   }

   public static boolean supports(Screen screen) {
      return screen instanceof HandledScreen && !(screen instanceof CreativeInventoryScreen);
   }

   public GlassSearchField attach(Screen screen) {
      if (this.isEnabled() && supports(screen)) {
         String previous = this.owner == screen && this.box != null ? this.box.value() : "";
         this.box = new GlassSearchField(Text.translatable("cloudvisuals.container_search.hint"));
         this.box.setValue(previous);
         this.owner = screen;
         return this.box;
      } else {
         this.box = null;
         this.owner = null;
         return null;
      }
   }

   public GlassSearchField box(Screen screen) {
      return this.owner == screen ? this.box : null;
   }

   public void place(Screen screen, int left, int top, int imageWidth) {
      GlassSearchField current = this.box(screen);
      if (current != null) {
         current.setX(left + imageWidth - 104 + 2);
         current.setY(Math.max(2, top - 16 - 9));
      }
   }

   public boolean typing(Screen screen) {
      GlassSearchField current = this.box(screen);
      return current != null && current.isFocused();
   }

   public String query(Screen screen) {
      GlassSearchField current = this.box(screen);
      return current == null ? "" : current.value().trim().toLowerCase(Locale.ROOT);
   }

   public static boolean matches(ItemStack stack, String query) {
      if (stack.isEmpty()) {
         return false;
      } else {
         return stack.getName().getString().toLowerCase(Locale.ROOT).contains(query) ? true : Registries.ITEM.getId(stack.getItem()).getPath().contains(query);
      }
   }
}
