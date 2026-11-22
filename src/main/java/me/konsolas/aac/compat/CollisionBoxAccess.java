package me.konsolas.aac.compat;


import me.konsolas.aac.movement.EntityDimensions;
import me.konsolas.aac.util.BoundingBox;
import me.konsolas.aac.util.CollisionResult;
import me.konsolas.aac.util.CollisionUtils;
import me.konsolas.aac.util.GroundCollisionState;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class CollisionBoxAccess {
   private static final MethodHandle AABB_MIN_X;
   private static final MethodHandle AABB_MIN_Y;
   private static final MethodHandle AABB_MIN_Z;
   private static final MethodHandle AABB_MAX_X;
   private static final MethodHandle AABB_MAX_Y;
   private static final MethodHandle AABB_MAX_Z;
   private static final MethodHandle WORLD_GET_HANDLE;
   private static final MethodHandle WORLD_GET_CUBES;
   private static final MethodHandle WORLD_GET_VOXEL_SHAPES;
   private static final MethodHandle VOXEL_SHAPE_TO_BOXES;
   private static final MethodHandle AABB_CONSTRUCTOR;
   private static final Vector GROUND_CHECK_GRAVITY;

   public static boolean isValidCoordinates(Location location) {
      return location == null
         ? false
         : Double.isFinite(location.getX())
            && Double.isFinite(location.getY())
            && Double.isFinite(location.getZ())
            && !(location.getY() > 32767.0)
            && !(location.getY() < -32767.0)
            && !(location.getX() >= 3.0E7)
            && !(location.getX() <= -3.0E7)
            && !(location.getZ() >= 3.0E7)
            && !(location.getZ() <= -3.0E7)
            && !(location.getX() >= 3.0E7)
            && !(location.getX() <= -3.0E7)
            && !(location.getZ() >= 3.0E7)
            && !(location.getZ() <= -3.0E7);
   }

   private static Object createNmsAxisAlignedBB(BoundingBox box) {
      try {
         return (Object)AABB_CONSTRUCTOR.invoke(
            (double)box.getMin().getX(),
            (double)box.getMin().getY(),
            (double)box.getMin().getZ(),
            (double)box.getMax().getX(),
            (double)box.getMax().getY(),
            (double)box.getMax().getZ()
         );
      } catch (Throwable ex) {
         rethrowCollisionError(ex);
         return null;
      }
   }

   private static void rethrowCollisionError(Throwable throwable) {
      throw new RuntimeException("Reflection error while pulling cubes from NMS", throwable);
   }

   public static List<BoundingBox> getCubes(Entity entity, BoundingBox box) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Must be called from the main thread");
      }

      if (isValidCoordinates(entity.getLocation())
         && isValidCoordinates(box.getMin().toLocation(entity.getWorld()))
         && isValidCoordinates(box.getMax().toLocation(entity.getWorld()))) {
         box = box.clampY(0.0, 256.0);
         if (box.getVolume() == 0.0) {
            return Collections.emptyList();
         }

         if (box.getVolume() > 10000.0) {
            Bukkit.getLogger().warning("[AAC] Tried to analyse bounding box with volume=" + box.getVolume());
            Bukkit.getLogger().warning("[AAC] bb=" + box);
            return Collections.emptyList();
         }

         World world = entity.getLocation().getWorld();

         Object obj;
         Object obj2;
         Object obj3;
         try {
            obj = (Object)WORLD_GET_HANDLE.invoke((World)world);
            obj2 = (Object)ProtocolVersion.GET_ENTITY_HANDLE_METHOD.invoke((Entity)entity);
            BoundingBox box2 = box;
            obj3 = createNmsAxisAlignedBB(box2);
         } catch (Throwable ex) {
            rethrowCollisionError(ex);
            return Collections.emptyList();
         }

         List<?> list;
         if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)) {
            try {
               Stream<?> stream = (Stream<?>)WORLD_GET_VOXEL_SHAPES.invoke((Object)obj, (Object)obj2, (Object)obj3);
               List<?> list2 = stream.flatMap(obj4 -> {
                  try {
                     return ((List<?>)VOXEL_SHAPE_TO_BOXES.invoke(obj4)).stream();
                  } catch (Throwable ex2) {
                     throw new RuntimeException(ex2);
                  }
               }).collect(Collectors.toList());
               list = list2;
            } catch (Throwable ex3) {
               rethrowCollisionError(ex3);
               return Collections.emptyList();
            }
         } else {
            try {
               list = (List<?>)WORLD_GET_CUBES.invoke((Object)obj, (Object)obj2, (Object)obj3);
            } catch (Throwable ex4) {
               rethrowCollisionError(ex4);
               return Collections.emptyList();
            }
         }

         ArrayList<BoundingBox> list3 = new ArrayList<>();

         try {
            for (Object obj5 : list) {
               list3.add(
                  new BoundingBox(
                     (double)AABB_MIN_X.invoke((Object)obj5),
                     (double)AABB_MIN_Y.invoke((Object)obj5),
                     (double)AABB_MIN_Z.invoke((Object)obj5),
                     (double)AABB_MAX_X.invoke((Object)obj5),
                     (double)AABB_MAX_Y.invoke((Object)obj5),
                     (double)AABB_MAX_Z.invoke((Object)obj5)
                  )
               );
            }

            return list3;
         } catch (Throwable ex5) {
            rethrowCollisionError(ex5);
            return Collections.emptyList();
         }
      } else {
         return Collections.emptyList();
      }
   }

   public static GroundCollisionState isToGround(Entity entity, Location location, Location location2, Vector vector, GroundCollisionState groundCollisionState) {
      vector = vector.clone();
      location = location.clone();
      vector = vector.multiply(new Vector(1.0001, vector.getY() < 0.0 ? 1.0001 : 1.0, 1.0001)).add(GROUND_CHECK_GRAVITY);
      Vector vector2 = vector.clone();
      Vector vector3 = vector;
      Location location3 = location;
      Entity entity2 = entity;
      CollisionResult collisionResult = CollisionUtils.traceCollisions(entity2, location3, vector3);
      boolean y = !collisionResult.getCollisionBoxes().isEmpty() && collisionResult.getDistance() < 0.6 + -GROUND_CHECK_GRAVITY.getY() && collisionResult.getDistance() > Vector.getEpsilon();
      boolean y2 = vector.getY() > vector2.getY() || groundCollisionState.isOnGround() && y;
      Vector vector4 = location2.toVector();
      Entity entity3 = entity;
      BoundingBox box = new BoundingBox(vector4, EntityDimensions.forEntity(entity3));
      boolean flag = collisionResult.getCollisionBoxes().stream().anyMatch(box2 -> box2.intersects(box));
      return GroundCollisionState.of(y2, y, flag);
   }

   public static GroundCollisionState checkMoveGroundCollision(PlayerMoveEvent event, GroundCollisionState groundCollisionState) {
      if (!event.getTo().getWorld().equals(event.getFrom().getWorld())) {
         return GroundCollisionState.TRUE_NORMAL;
      }

      Player player = event.getPlayer();
      Vector vector = event.getTo().clone().subtract(event.getFrom()).toVector();
      return isToGround(player, event.getFrom(), event.getTo(), vector, groundCollisionState);
   }

   public static boolean isOnBlockSurface(Player player, Location location) {
      Vector vector = location.clone().subtract(0.0, 0.001, 0.0).toVector();
      Player player2 = player;
      BoundingBox box = new BoundingBox(vector, EntityDimensions.forEntity(player2));
      Player player3 = player;
      List<BoundingBox> list = getCubes(player3, box);
      if (list.isEmpty()) {
         return false;
      }

      for (BoundingBox box2 : list) {
         if (box2.getMax().getY() == location.getY()) {
            return true;
         }
      }

      return false;
   }

   public static boolean anyBlockMatches(World world, BoundingBox box, Predicate<Block> predicate) {
      box = box.clampY(0.0, 256.0);
      if (box.getVolume() == 0.0) {
         return false;
      }

      int x = (int)Math.floor(box.getMin().getX());
      int y = (int)Math.floor(box.getMin().getY());
      int z = (int)Math.floor(box.getMin().getZ());
      int x2 = (int)Math.floor(box.getMax().getX() + 1.0);
      int y2 = (int)Math.floor(box.getMax().getY() + 1.0);
      int z2 = (int)Math.floor(box.getMax().getZ() + 1.0);

      for (int value = x; value < x2; value++) {
         for (int value2 = y; value2 < y2; value2++) {
            for (int value3 = z; value3 < z2; value3++) {
               if (predicate.test(world.getBlockAt(value, value2, value3))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static int countMatchingBlocks(World world, BoundingBox box, Predicate<Block> predicate) {
      box = box.clampY(0.0, 256.0);
      if (box.getVolume() == 0.0) {
         return 0;
      }

      int x = (int)Math.floor(box.getMin().getX());
      int y = (int)Math.floor(box.getMin().getY());
      int z = (int)Math.floor(box.getMin().getZ());
      int x2 = (int)Math.floor(box.getMax().getX() + 1.0);
      int y2 = (int)Math.floor(box.getMax().getY() + 1.0);
      int z2 = (int)Math.floor(box.getMax().getZ() + 1.0);
      int value = 0;

      for (int value2 = x; value2 < x2; value2++) {
         for (int value3 = y; value3 < y2; value3++) {
            for (int value4 = z; value4 < z2; value4++) {
               if (predicate.test(world.getBlockAt(value2, value3, value4))) {
                  value++;
               }
            }
         }
      }

      return value;
   }

   static {
      try {
         Class classVal = ReflectionUtils.getNmsEntityClass();
         String str = "AxisAlignedBB";
         Class classVal2 = ReflectionUtils.getNmsClass(str);
         Class classVal3 = ReflectionUtils.getNmsWorldClass();
         Lookup lookup = ReflectionAccess.LOOKUP;
         Class classVal4 = ReflectionUtils.getCraftWorldClass();
         Class[] classValArray = new Class[0];
         WORLD_GET_HANDLE = lookup.unreflect(
            (Method)ReflectionAccess.makeAccessible(classVal4.getMethod("getHandle", classValArray))
         );
         if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)) {
            AABB_MIN_X = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal2.getDeclaredField("minX")));
            AABB_MIN_Y = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal2.getDeclaredField("minY")));
            AABB_MIN_Z = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal2.getDeclaredField("minZ")));
            AABB_MAX_X = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal2.getDeclaredField("maxX")));
            AABB_MAX_Y = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal2.getDeclaredField("maxY")));
            AABB_MAX_Z = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal2.getDeclaredField("maxZ")));
         } else {
            AABB_MIN_X = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal2.getDeclaredField("a")));
            AABB_MIN_Y = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal2.getDeclaredField("b")));
            AABB_MIN_Z = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal2.getDeclaredField("c")));
            AABB_MAX_X = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal2.getDeclaredField("d")));
            AABB_MAX_Y = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal2.getDeclaredField("e")));
            AABB_MAX_Z = ReflectionAccess.LOOKUP.unreflectGetter((Field)ReflectionAccess.makeAccessible(classVal2.getDeclaredField("f")));
         }

         if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)) {
            lookup = ReflectionAccess.LOOKUP;
            Class[] classValArray2 = new Class[]{classVal, classVal2};
            WORLD_GET_VOXEL_SHAPES = lookup.unreflect(
               (Method)ReflectionAccess.makeAccessible(classVal3.getMethod("b", classValArray2))
            );
            lookup = ReflectionAccess.LOOKUP;
            String str2 = "VoxelShape";
            classVal4 = ReflectionUtils.getNmsClass(str2);
            classValArray = new Class[0];
            VOXEL_SHAPE_TO_BOXES = lookup.unreflect(
               (Method)ReflectionAccess.makeAccessible(classVal4.getMethod("d", classValArray))
            );
            WORLD_GET_CUBES = null;
         } else {
            WORLD_GET_VOXEL_SHAPES = null;
            VOXEL_SHAPE_TO_BOXES = null;
            lookup = ReflectionAccess.LOOKUP;
            Class[] classValArray3 = new Class[]{classVal, classVal2};
            WORLD_GET_CUBES = lookup.unreflect(
               (Method)ReflectionAccess.makeAccessible(classVal3.getMethod("getCubes", classValArray3))
            );
         }

         AABB_CONSTRUCTOR = ReflectionAccess.LOOKUP
            .unreflectConstructor(
               (Constructor<?>)ReflectionAccess.makeAccessible(
                  classVal2.getConstructor(double.class, double.class, double.class, double.class, double.class, double.class)
               )
            );
      } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException ex) {
         rethrowCollisionError(ex);
         throw new IllegalStateException();
      }

      GROUND_CHECK_GRAVITY = new Vector(0.0, -0.08, 0.0);
   }
}
