package me.konsolas.aac.movement;


import me.konsolas.aac.compat.ProtocolVersion;
import me.konsolas.aac.compat.ReflectionAccess;
import me.konsolas.aac.compat.ReflectionUtils;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public class EntityDimensions {
   private static final EntityDimensions DEFAULT_DIMENSIONS = new EntityDimensions(0.6, 20.0);
   public static final EntityDimensions STANDING_PLAYER = new EntityDimensions(0.6, 1.8);
   public static final EntityDimensions SNEAKING_PLAYER = new EntityDimensions(0.6, 1.65);
   public static final EntityDimensions ELYTRA_PLAYER = new EntityDimensions(0.6, 0.6);
   public static final EntityDimensions SWIMMING_PLAYER = new EntityDimensions(0.6, 0.6);
   private static MethodHandle ENTITY_SIZE_FIELD;
   private static MethodHandle WIDTH_FIELD;
   private static MethodHandle HEIGHT_FIELD;
   private final double width;
   private final double height;

   public EntityDimensions(double d, double d2) {
      this.width = d;
      this.height = d2;
   }

   private static EntityDimensions lookupForEntity(Entity entity) {
      double d;
      double d2;
      try {
         Object obj = (Object)ProtocolVersion.GET_ENTITY_HANDLE_METHOD.invoke((Entity)entity);
         if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_14)) {
            Object obj2 = (Object)ENTITY_SIZE_FIELD.invoke((Object)obj);
            d = (float)WIDTH_FIELD.invoke((Object)obj2);
            d2 = (float)HEIGHT_FIELD.invoke((Object)obj2);
         } else {
            d = (float)WIDTH_FIELD.invoke((Object)obj);
            d2 = (float)HEIGHT_FIELD.invoke((Object)obj);
         }
      } catch (Throwable ex) {
         Bukkit.getLogger().warning("[AAC] Failed to pull entity hitboxes from NMS. Using default hitbox. (" + ex.getMessage() + ")");
         return DEFAULT_DIMENSIONS;
      }

      return new EntityDimensions(d, d2);
   }

   public static EntityDimensions forEntity(Entity entity) {
      Entity entity2 = entity;
      return lookupForEntity(entity2);
   }

   public double getWidth() {
      return this.width;
   }

   public double getHeight() {
      return this.height;
   }

   static {
      try {
         if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_14)) {
            ENTITY_SIZE_FIELD = ReflectionAccess.LOOKUP
               .unreflectGetter((Field)ReflectionAccess.makeAccessible(ReflectionUtils.getNmsEntityClass().getDeclaredField("size")));
            String str = "EntitySize";
            Class classVal = ReflectionUtils.getNmsClass(str);
            WIDTH_FIELD = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal.getDeclaredField("width")));
            HEIGHT_FIELD = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal.getDeclaredField("height")));
         } else {
            WIDTH_FIELD = ReflectionAccess.LOOKUP
               .unreflectGetter((Field)ReflectionAccess.makeAccessible(ReflectionUtils.getNmsEntityClass().getDeclaredField("width")));
            HEIGHT_FIELD = ReflectionAccess.LOOKUP
               .unreflectGetter((Field)ReflectionAccess.makeAccessible(ReflectionUtils.getNmsEntityClass().getDeclaredField("length")));
         }
      } catch (NoSuchFieldException | IllegalAccessException ex) {
         throw new RuntimeException(ex);
      }
   }
}
