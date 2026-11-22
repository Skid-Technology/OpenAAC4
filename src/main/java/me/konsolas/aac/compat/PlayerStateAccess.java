package me.konsolas.aac.compat;


import me.konsolas.aac.movement.WaterContactState;
import me.konsolas.aac.util.BoundingBox;
import me.konsolas.aac.util.MaterialGroup;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerStateAccess {
   private static final Set BLOCKABLE_MATERIALS;
   private static final MethodHandle ACTIVE_ITEM_HANDLE;
   private static final Object EMPTY_ITEM_STACK;
   private static final MethodHandle ENTITY_GET_FLAG;
   private static final MethodHandle BLOCK_GET_NMS;
   private static final MethodHandle BLOCK_FRICTION_FACTOR;
   private static Set WATER_BLOCKS;

   public static boolean isUsingItem(Player player) {
      try {
         Object obj = (Object)ProtocolVersion.GET_ENTITY_HANDLE_METHOD.invoke((Player)player);
         switch (ProtocolVersionSwitch.ordinalSwitch[ProtocolVersion.getServerVersion().ordinal()]) {
            case 1:
               return (boolean)ACTIVE_ITEM_HANDLE.invoke((Object)obj);
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
               return !Objects.equals((Object)ACTIVE_ITEM_HANDLE.invoke((Object)obj), EMPTY_ITEM_STACK);
         }
      } catch (Throwable ex) {
         Bukkit.getLogger()
            .warning("[AAC] Failed to isSet item use state for " + player.getName() + " " + ProtocolVersion.getServerVersion() + ":" + ex.getMessage());
      }

      return false;
   }

   public static boolean isGliding(Player player) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Must be called from the main thread");
      }

      if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_9)) {
         try {
            Object obj = (Object)ProtocolVersion.GET_ENTITY_HANDLE_METHOD.invoke((Player)player);
            return (boolean)ENTITY_GET_FLAG.invoke((Object)obj, (int)7);
         } catch (Throwable ex) {
            ex.printStackTrace();
         }
      }

      return false;
   }

   public static List<ItemStack> getHeldItems(Player player) {
      if (ProtocolVersion.getServerVersion() == ProtocolVersion.V1_8) {
         ItemStack item = player.getItemInHand();
         return Collections.singletonList(item);
      } else {
         return Arrays.asList(player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand());
      }
   }

   public static float getBlockSlipperiness(Block block) {
      try {
         return (float)BLOCK_FRICTION_FACTOR.invoke((Object)(Object)BLOCK_GET_NMS.invoke((Block)block));
      } catch (Throwable ex) {
         throw new RuntimeException(ex);
      }
   }

   private static int getWaterLevel(Block block) {
      int i;
      if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)) {
         if (block.getType() == Material.BUBBLE_COLUMN) {
            return 0;
         }

         Levelled levelled = (Levelled)block.getBlockData();
         i = levelled.getLevel();
      } else {
         i = block.getData();
      }

      return i;
   }

   private static float getLiquidHeight(int i) {
      if (i >= 8) {
         i = 0;
      }

      return (i + 1) / 9.0F;
   }

   private static Optional getWaterDataLevel(Block block, ProtocolVersion protocolVersion) {
      if (block.isLiquid()) {
         return Optional.of(getWaterLevel(block));
      }

      if (protocolVersion.isAtLeast(ProtocolVersion.V1_13) && ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)) {
         if (block.getBlockData() instanceof Waterlogged && ((Waterlogged)block.getBlockData()).isWaterlogged()) {
            return Optional.of(0);
         }

         if (WATER_BLOCKS.contains(block.getType())) {
            return Optional.of(0);
         }
      }

      return Optional.empty();
   }

   public static WaterContactState checkWaterContact(World world, BoundingBox box, ProtocolVersion protocolVersion) {
      int x = (int)Math.floor(box.getMin().getX());
      int x2 = (int)Math.floor(box.getMax().getX() + 1.0);
      int y = (int)Math.floor(box.getMin().getY());
      int y2 = (int)Math.floor(box.getMax().getY() + 1.0);
      int z = (int)Math.floor(box.getMin().getZ());
      int z2 = (int)Math.floor(box.getMax().getZ() + 1.0);
      WaterContactState waterContactState = new WaterContactState();
      if (protocolVersion.isAtLeast(ProtocolVersion.V1_13)) {
         for (int value = x; value < x2; value++) {
            for (int value2 = y; value2 < y2; value2++) {
               for (int value3 = z; value3 < z2; value3++) {
                  Block block = world.getBlockAt(value, value2, value3);
                  if (!MaterialGroup.LAVA.contains(block.getType())) {
                     Optional optional = getWaterDataLevel(block, protocolVersion);
                     if (optional.isPresent()) {
                        double d = value2 + 1 - getLiquidHeight((Integer)optional.get());
                        if (d >= box.getMin().getY()) {
                           waterContactState.intersectsWater = true;
                           waterContactState.immersionDepth = Math.max(d - box.getMin().getY(), waterContactState.immersionDepth);
                           if ((Integer)optional.get() != 0) {
                              waterContactState.intersectsFlowingWater = true;
                           }
                        }
                     }
                  }
               }
            }
         }
      } else {
         for (int value4 = x; value4 < x2; value4++) {
            for (int value5 = y; value5 < y2; value5++) {
               for (int value6 = z; value6 < z2; value6++) {
                  Block block2 = world.getBlockAt(value4, value5, value6);
                  if (!MaterialGroup.LAVA.contains(block2.getType())) {
                     Optional optional2 = getWaterDataLevel(block2, protocolVersion);
                     if (optional2.isPresent()) {
                        double d2 = value5 + 1 - getLiquidHeight((Integer)optional2.get());
                        if (y2 >= d2) {
                           waterContactState.intersectsWater = true;
                           if ((Integer)optional2.get() != 0) {
                              waterContactState.intersectsFlowingWater = true;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return waterContactState;
   }

   static {
      if (ProtocolVersion.getServerVersion() == ProtocolVersion.V1_8) {
         BLOCKABLE_MATERIALS = EnumSet.of(
            Material.getMaterial("STONE_SWORD"),
            Material.getMaterial("DIAMOND_SWORD"),
            Material.getMaterial("IRON_SWORD"),
            Material.getMaterial("GOLD_SWORD"),
            Material.getMaterial("WOOD_SWORD"),
            Material.getMaterial("BOW")
         );
      } else {
         BLOCKABLE_MATERIALS = EnumSet.of(Material.getMaterial("SHIELD"), Material.getMaterial("BOW"));
      }

      try {
         switch (ProtocolVersionSwitch.ordinalSwitch[ProtocolVersion.getServerVersion().ordinal()]) {
            case 1:
               Lookup lookup = ReflectionAccess.LOOKUP;
               Class classVal = ReflectionUtils.getNmsEntityPlayerClass().getSuperclass();
               Class[] classValArray = new Class[0];
               ACTIVE_ITEM_HANDLE = lookup.unreflect(
                  (Method)ReflectionAccess.makeAccessible(
                     classVal.getDeclaredMethod("bS", classValArray)
                  )
               );
               EMPTY_ITEM_STACK = null;
               break;
            case 2:
               ACTIVE_ITEM_HANDLE = ReflectionAccess.LOOKUP
                  .unreflectGetter(
                     (Field)ReflectionAccess.makeAccessible(ReflectionUtils.getNmsEntityPlayerClass().getSuperclass().getSuperclass().getDeclaredField("bn"))
                  );
               EMPTY_ITEM_STACK = null;
               break;
            case 3:
               ACTIVE_ITEM_HANDLE = ReflectionAccess.LOOKUP
                  .unreflectGetter(
                     (Field)ReflectionAccess.makeAccessible(ReflectionUtils.getNmsEntityPlayerClass().getSuperclass().getSuperclass().getDeclaredField("bo"))
                  );
               EMPTY_ITEM_STACK = null;
               break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
               ACTIVE_ITEM_HANDLE = ReflectionAccess.LOOKUP
                  .unreflectGetter(
                     (Field)ReflectionAccess.makeAccessible(ReflectionUtils.getNmsEntityPlayerClass().getSuperclass().getSuperclass().getDeclaredField("activeItem"))
                  );
               EMPTY_ITEM_STACK = ReflectionUtils.getNmsItemStackClass().getField("a").get(null);
               break;
            default:
               throw new AssertionError();
         }

         if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_9)) {
            Lookup lookup2 = ReflectionAccess.LOOKUP;
            Class classVal2 = ReflectionUtils.getNmsEntityClass();
            Class[] classValArray2 = new Class[]{int.class};
            ENTITY_GET_FLAG = lookup2.unreflect(
               (Method)ReflectionAccess.makeAccessible(classVal2.getMethod("getFlag", classValArray2))
            );
         } else {
            ENTITY_GET_FLAG = null;
         }

         Lookup lookup3 = ReflectionAccess.LOOKUP;
         Class classVal3 = ReflectionUtils.getCraftBlockClass();
         Class[] classValArray3 = new Class[0];
         BLOCK_GET_NMS = lookup3.unreflect(
            (Method)ReflectionAccess.makeAccessible(classVal3.getDeclaredMethod("getNMSBlock", classValArray3))
         );
         BLOCK_FRICTION_FACTOR = ReflectionAccess.LOOKUP
            .unreflectGetter((Field)ReflectionAccess.makeAccessible(ReflectionUtils.getNmsBlockClass().getDeclaredField("frictionFactor")));
      } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException ex) {
         throw new IllegalStateException(ex);
      }

      WATER_BLOCKS = EnumSet.noneOf(Material.class);

      for (String str : Arrays.asList(
         "BUBBLE_COLUMN", "KELP", "KELP_PLANT", "SEAGRASS", "TALL_SEAGRASS", "TUBE_CORAL", "BRAIN_CORAL", "BUBBLE_CORAL", "FIRE_CORAL", "CONDUIT"
      )) {
         try {
            WATER_BLOCKS.add(Material.valueOf(str));
         } catch (IllegalArgumentException ex2) {
         }
      }
   }
}
