package me.konsolas.aac.compat;


import me.konsolas.aac.util.RelativeBlockAccess;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlockBreakAccess {
   private static final Method GET_PLAYER_HANDLE;
   private static final Method GET_WORLD_HANDLE;
   private static final Constructor BLOCK_POS_CONSTRUCTOR;
   private static final Method WORLD_GET_TYPE;
   private static Method BLOCK_DATA_GET_BLOCK;
   private static Method BLOCK_GET_DAMAGE_1_8;
   private static Method BLOCK_DATA_GET_DAMAGE_LEGACY;
   private static Method BLOCK_DATA_GET_DAMAGE;

   public static long getBreakTicks(Block block, Player player) {
      return (long)(1.0F / getBlockDamagePerTick(block, player));
   }

   private static float getBlockDamagePerTick(Block block, Player player) {
      RelativeBlockAccess.unusedFlag();
      if (ProtocolVersion.getServerVersion() == ProtocolVersion.V1_8) {
         try {
            Object x = BLOCK_POS_CONSTRUCTOR.newInstance(block.getX(), block.getY(), block.getZ());
            Object obj = GET_WORLD_HANDLE.invoke(block.getWorld());
            Object obj2 = WORLD_GET_TYPE.invoke(obj, x);
            Object obj3 = BLOCK_DATA_GET_BLOCK.invoke(obj2);
            return (Float)BLOCK_GET_DAMAGE_1_8.invoke(obj3, GET_PLAYER_HANDLE.invoke(player), obj, x);
         } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
         }
      } else if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)) {
         try {
            Object x2 = BLOCK_POS_CONSTRUCTOR.newInstance(block.getX(), block.getY(), block.getZ());
            Object obj4 = GET_WORLD_HANDLE.invoke(block.getWorld());
            Object obj5 = WORLD_GET_TYPE.invoke(obj4, x2);
            return (Float)BLOCK_DATA_GET_DAMAGE.invoke(obj5, GET_PLAYER_HANDLE.invoke(player), obj4, x2);
         } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex2) {
            throw new RuntimeException(ex2);
         }
      } else {
         try {
            Object x3 = BLOCK_POS_CONSTRUCTOR.newInstance(block.getX(), block.getY(), block.getZ());
            Object obj6 = GET_WORLD_HANDLE.invoke(block.getWorld());
            Object obj7 = WORLD_GET_TYPE.invoke(obj6, x3);
            return (Float)BLOCK_DATA_GET_DAMAGE_LEGACY.invoke(obj7, GET_PLAYER_HANDLE.invoke(player), obj6, x3);
         } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex3) {
            throw new RuntimeException(ex3);
         }
      }
   }

   public static int getHasteAmplifier(Player player) {

      for (PotionEffect potionEffect : player.getActivePotionEffects()) {
         if (potionEffect.getType().equals(PotionEffectType.FAST_DIGGING)) {
            return potionEffect.getAmplifier() + 1;
         }
      }

      return 0;
   }

   static {

      try {
         Class classVal = ReflectionUtils.getCraftWorldClass();
         Class[] classValArray = new Class[0];
         GET_WORLD_HANDLE = classVal.getMethod("getHandle", classValArray);
         Class classVal2 = ReflectionUtils.getCraftPlayerClass();
         classValArray = new Class[0];
         GET_PLAYER_HANDLE = classVal2.getMethod("getHandle", classValArray);
         BLOCK_POS_CONSTRUCTOR = ReflectionUtils.getNmsBlockPositionClass().getConstructor(int.class, int.class, int.class);
         Class classVal3 = ReflectionUtils.getNmsWorldClass();
         classValArray = new Class[]{ReflectionUtils.getNmsBlockPositionClass()};
         WORLD_GET_TYPE = classVal3.getMethod("getType", classValArray);
         if (ProtocolVersion.getServerVersion() == ProtocolVersion.V1_8) {
            Class classVal4 = ReflectionUtils.getNmsIBlockDataClass();
            classValArray = new Class[0];
            BLOCK_DATA_GET_BLOCK = classVal4.getMethod("getBlock", classValArray);
            Class classVal5 = ReflectionUtils.getNmsBlockClass();
            classValArray = new Class[]{ReflectionUtils.getNmsEntityHumanClass(), ReflectionUtils.getNmsWorldClass(), ReflectionUtils.getNmsBlockPositionClass()};
            BLOCK_GET_DAMAGE_1_8 = classVal5.getMethod("getDamage", classValArray);
         } else if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)) {
            Class classVal6 = ReflectionUtils.getNmsIBlockDataClass();
            classValArray = new Class[]{ReflectionUtils.getNmsEntityHumanClass(), ReflectionUtils.getNmsIBlockAccessClass(), ReflectionUtils.getNmsBlockPositionClass()};
            BLOCK_DATA_GET_DAMAGE = classVal6.getMethod("getDamage", classValArray);
         } else {
            Class classVal7 = ReflectionUtils.getNmsIBlockDataClass();
            classValArray = new Class[]{ReflectionUtils.getNmsEntityHumanClass(), ReflectionUtils.getNmsWorldClass(), ReflectionUtils.getNmsBlockPositionClass()};
            BLOCK_DATA_GET_DAMAGE_LEGACY = classVal7.getMethod("a", classValArray);
         }
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }
}
