package me.konsolas.aac.compat;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;

public final class ReflectionUtils {
   private static String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();
   private static String NMS_PACKAGE = CRAFTBUKKIT_PACKAGE.replace("org.bukkit.craftbukkit", "net.minecraft.server");
   private static final Map CLASS_CACHE = new HashMap();

   private static Class loadClass(String str) {
      try {
         if (CLASS_CACHE.containsKey(str)) {
            return (Class)CLASS_CACHE.get(str);
         }

         Class classVal = Class.forName(str);
         CLASS_CACHE.put(str, classVal);
         return classVal;
      } catch (ClassNotFoundException ex) {
         throw new IllegalArgumentException("Class not found: " + str, ex);
      }
   }

   public static Class getNmsClass(String str) {
      String str2 = NMS_PACKAGE + '.' + str;
      return loadClass(str2);
   }

   public static Class getCraftBukkitClass(String str) {
      String str2 = CRAFTBUKKIT_PACKAGE + '.' + str;
      return loadClass(str2);
   }

   public static Class getCraftEntityClass() {
      String str = "entity.CraftEntity";
      return getCraftBukkitClass(str);
   }

   public static Class getCraftPlayerClass() {
      String str = "entity.CraftPlayer";
      return getCraftBukkitClass(str);
   }

   public static Class getCraftWorldClass() {
      String str = "CraftWorld";
      return getCraftBukkitClass(str);
   }

   public static Class getCraftBlockClass() {
      String str = "block.CraftBlock";
      return getCraftBukkitClass(str);
   }

   public static Class getNmsEntityClass() {
      String str = "Entity";
      return getNmsClass(str);
   }

   public static Class getNmsEntityPlayerClass() {
      String str = "EntityPlayer";
      return getNmsClass(str);
   }

   public static Class getNmsEntityHumanClass() {
      String str = "EntityHuman";
      return getNmsClass(str);
   }

   public static Class getNmsWorldClass() {
      String str = "World";
      return getNmsClass(str);
   }

   public static Class getNmsItemStackClass() {
      String str = "ItemStack";
      return getNmsClass(str);
   }

   public static Class getNmsBlockClass() {
      String str = "Block";
      return getNmsClass(str);
   }

   public static Class getNmsBlockPositionClass() {
      String str = "BlockPosition";
      return getNmsClass(str);
   }

   public static Class getNmsIBlockDataClass() {
      String str = "IBlockData";
      return getNmsClass(str);
   }

   public static Class getNmsIBlockAccessClass() {
      String str = "IBlockAccess";
      return getNmsClass(str);
   }

   public static Class getNmsVec3DClass() {
      String str = "Vec3D";
      return getNmsClass(str);
   }


   public static int unusedVersionId() {
      return 69;
   }
}
