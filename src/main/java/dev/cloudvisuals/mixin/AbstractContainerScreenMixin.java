package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.cloudvisuals.gui.UiSounds;
import dev.cloudvisuals.gui.screen.GlassSearchField;
import dev.cloudvisuals.gui.screen.InventoryDesign;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.ContainerSearchModule;
import dev.cloudvisuals.module.impl.ItemLocksModule;
import dev.cloudvisuals.module.impl.ScreensModule;
import dev.cloudvisuals.module.impl.visual.ContainerAnimationModule;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class AbstractContainerScreenMixin {
   @Shadow
   protected int backgroundWidth;
   @Shadow
   protected int backgroundHeight;
   @Shadow
   protected int x;
   @Shadow
   protected int y;
   @Shadow
   protected Slot focusedSlot;
   @Unique
   private long cloudvisuals$openedAt;

   public AbstractContainerScreenMixin() {
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void cloudvisuals$opened(CallbackInfo ci) {
      if (this.cloudvisuals$openedAt == 0L) {
         this.cloudvisuals$openedAt = System.currentTimeMillis();
      }
   }

   @WrapOperation(
      method = "renderBackground",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V")
   )
   private void cloudvisuals$animateBackground(HandledScreen<?> screen, DrawContext g, float partialTick, int mouseX, int mouseY, Operation<Void> original) {
      ContainerAnimationModule module = Modules.containerAnimation();
      float progress = module == null ? 1.0F : module.progress(this.cloudvisuals$openedAt);
      if (module != null && !(progress >= 1.0F)) {
         g.getMatrices().pushMatrix();
         g.getMatrices().translate(this.x, this.y);
         module.apply(g.getMatrices(), this.backgroundWidth, this.backgroundHeight, progress);
         g.getMatrices().translate(-this.x, -this.y);
         this.cloudvisuals$background(screen, g, partialTick, mouseX, mouseY, original);
         g.getMatrices().popMatrix();
      } else {
         this.cloudvisuals$background(screen, g, partialTick, mouseX, mouseY, original);
      }
   }

   @Unique
   private void cloudvisuals$background(HandledScreen<?> screen, DrawContext g, float partialTick, int mouseX, int mouseY, Operation<Void> original) {
      if (InventoryDesign.handles(screen)) {
         try {
            InventoryDesign.renderBackground(screen, g, this.x, this.y, this.backgroundWidth, this.backgroundHeight, mouseX, mouseY);
            return;
         } catch (RuntimeException e) {
            ScreensModule.crashed(e);
         }
      }

      original.call(new Object[]{screen, g, partialTick, mouseX, mouseY});
   }

   @ModifyArg(
      method = "drawForeground",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V"
      ),
      index = 4
   )
   private int cloudvisuals$labelColor(int color) {
      return InventoryDesign.handles((HandledScreen<?>)(Object)this) ? -1644564 : color;
   }

   @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$keys(KeyInput event, CallbackInfoReturnable<Boolean> cir) {
      int key = event.key();
      HandledScreen<?> screen = (HandledScreen<?>)(Object)this;
      ContainerSearchModule search = Modules.containerSearch();
      if (search != null && search.typing(screen)) {
         GlassSearchField box = search.box(screen);
         if (key != 256 && key != 257 && key != 335) {
            box.keyPressed(event);
         } else {
            if (key == 256) {
               box.setValue("");
            }

            box.setFocused(false);
            screen.setFocused(null);
         }

         cir.setReturnValue(true);
      } else {
         ItemLocksModule locks = Modules.itemLocks();
         if (locks != null && key == locks.lockKey() && locks.toggle(this.focusedSlot)) {
            UiSounds.play(UiSounds.Sound.CLICK);
            cir.setReturnValue(true);
         }
      }
   }

   @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$lockedThrow(Slot slot, int slotId, int button, SlotActionType type, CallbackInfo ci) {
      ItemLocksModule locks = Modules.itemLocks();
      if (type == SlotActionType.THROW && locks != null && locks.isLocked(slot)) {
         locks.notifyBlocked();
         ci.cancel();
      }
   }

   @Inject(method = "drawSlot", at = @At("TAIL"))
   private void cloudvisuals$slotOverlay(DrawContext g, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
      HandledScreen<?> screen = (HandledScreen<?>)(Object)this;
      ContainerSearchModule search = Modules.containerSearch();
      String query = search == null ? "" : search.query(screen);
      ItemLocksModule locks = Modules.itemLocks();
      boolean locked = locks != null && locks.isLocked(slot);
      if (!query.isEmpty() || locked) {
         InventoryDesign.renderSlotMarks(g, slot.x, slot.y, query.isEmpty() ? 0 : (ContainerSearchModule.matches(slot.getStack(), query) ? 1 : -1), locked);
      }
   }

   @Inject(method = "mouseClicked", at = @At("HEAD"))
   private void cloudvisuals$searchBlur(Click event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
      double mouseX = event.x();
      double mouseY = event.y();
      HandledScreen<?> screen = (HandledScreen<?>)(Object)this;
      ContainerSearchModule search = Modules.containerSearch();
      if (search != null && search.typing(screen)) {
         GlassSearchField box = search.box(screen);
         if (!box.isMouseOver(mouseX, mouseY)) {
            box.setFocused(false);
            screen.setFocused(null);
         }
      }
   }

   @Inject(method = "render", at = @At("HEAD"))
   private void cloudvisuals$placeSearch(DrawContext g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      ContainerSearchModule search = Modules.containerSearch();
      if (search != null) {
         search.place((HandledScreen)(Object)this, this.x, this.y, this.backgroundWidth);
      }
   }

   @Inject(method = "drawSlotHighlightBack", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$slotHighlight(DrawContext g, CallbackInfo ci) {
      HandledScreen<?> screen = (HandledScreen<?>)(Object)this;
      if (InventoryDesign.handles(screen)) {
         if (this.focusedSlot != null && this.focusedSlot.canBeHighlighted()) {
            InventoryDesign.renderSlotHighlight(g, this.focusedSlot.x, this.focusedSlot.y);
         }

         ci.cancel();
      }
   }

   @Inject(method = "drawSlotHighlightFront", at = @At("HEAD"), cancellable = true)
   private void cloudvisuals$slotHighlightFront(DrawContext g, CallbackInfo ci) {
      if (InventoryDesign.handles((HandledScreen<?>)(Object)this)) {
         ci.cancel();
      }
   }

   @Inject(
      method = "renderMain",
      at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;", ordinal = 0, shift = Shift.AFTER)
   )
   private void cloudvisuals$animateSlots(DrawContext g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      ContainerAnimationModule module = Modules.containerAnimation();
      if (module != null) {
         module.apply(g.getMatrices(), this.backgroundWidth, this.backgroundHeight, module.progress(this.cloudvisuals$openedAt));
      }
   }
}
