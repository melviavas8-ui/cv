package dev.cloudvisuals.util;

import java.lang.reflect.Field;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;

public final class EntityLayers {
   private static final EntityModelLayer PLAYER_SLIM = resolvePlayerSlim();

   private EntityLayers() {
   }

   private static EntityModelLayer resolvePlayerSlim() {
      for (Field field : EntityModelLayers.class.getFields()) {
         if (field.getName().equals("PLAYER_SLIM") && field.getType() == EntityModelLayer.class) {
            try {
               return (EntityModelLayer)field.get(null);
            } catch (ReflectiveOperationException e) {
               throw new IllegalStateException(e);
            }
         }
      }

      throw new IllegalStateException("EntityModelLayers.PLAYER_SLIM (EntityModelLayer) not found");
   }

   public static EntityModelLayer playerSlim() {
      return PLAYER_SLIM;
   }
}
