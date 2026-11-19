package dev.cloudvisuals.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.cloudvisuals.gui.ChatDecorator;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.visual.ChatModule;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.ChatHud.Backend;
import net.minecraft.client.gui.hud.ChatHudLine.Visible;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public abstract class ChatComponentMixin {
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   @Final
   private List<ChatHudLine> messages;
   @Shadow
   @Final
   private List<Visible> visibleMessages;
   @Unique
   private String cloudvisuals$lastText;
   @Unique
   private int cloudvisuals$repeats = 1;

   public ChatComponentMixin() {
   }

   @ModifyVariable(
      method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
      at = @At("HEAD"),
      argsOnly = true
   )
   private Text cloudvisuals$decorate(Text message) {
      ChatModule chat = Modules.chat();
      if (chat != null && chat.isEnabled()) {
         String text = message.getString();
         Text result = message;
         if (chat.merge() && text.equals(this.cloudvisuals$lastText) && !this.messages.isEmpty()) {
            this.cloudvisuals$repeats++;
            this.messages.remove(0);
            if (!this.visibleMessages.isEmpty()) {
               this.visibleMessages.remove(0);

               while (!this.visibleMessages.isEmpty() && !this.visibleMessages.get(0).endOfEntry()) {
                  this.visibleMessages.remove(0);
               }
            }

            result = message.copy().append(Text.literal(" \u00d7" + this.cloudvisuals$repeats).formatted(Formatting.GRAY));
         } else {
            this.cloudvisuals$repeats = 1;
         }

         this.cloudvisuals$lastText = text;
         return chat.decorate(result);
      } else {
         this.cloudvisuals$lastText = null;
         return message;
      }
   }

   @ModifyArg(
      method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;IIIZZ)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/gui/hud/ChatHud$Backend;IIZ)V"),
      index = 0
   )
   private Backend cloudvisuals$decorate(
      Backend access,
      @Local(argsOnly = true) DrawContext g,
      @Local(argsOnly = true, ordinal = 0) int tick,
      @Local(argsOnly = true, ordinal = 0) boolean focused
   ) {
      ChatModule chat = Modules.chat();
      if (chat == null || !chat.glass() && !chat.animation()) {
         return access;
      }

      float opacity = (float)((Double)this.client.options.getChatOpacity().getValue() * 0.9 + 0.1);
      return new ChatDecorator(access, g, chat, this.visibleMessages, tick, focused, opacity);
   }
}
