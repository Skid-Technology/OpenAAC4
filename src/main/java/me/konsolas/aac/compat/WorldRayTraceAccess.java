package me.konsolas.aac.compat;


import me.konsolas.aac.util.BlockRayTraceResult;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class WorldRayTraceAccess {
   private static final MethodHandle WORLD_GET_HANDLE;
   private static final MethodHandle VEC3D_CONSTRUCTOR;
   private static final MethodHandle HIT_GET_BLOCK_POS;
   private static final MethodHandle HIT_GET_DIRECTION;
   private static final MethodHandle NOTCH_TO_BLOCK_FACE;
   private static final MethodHandle HIT_GET_POS;
   private static final MethodHandle VEC_X;
   private static final MethodHandle VEC_Y;
   private static final MethodHandle VEC_Z;
   private static final MethodHandle POS_X;
   private static final MethodHandle POS_Y;
   private static final MethodHandle POS_Z;
   private static final MethodHandle WORLD_RAY_TRACE;

   public static BlockRayTraceResult rayTraceBlock(Entity entity, Vector vector, Vector vector2, double d) {
      if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)) {
         RayTraceResult rayTraceResult = entity.getWorld().rayTraceBlocks(vector.toLocation(entity.getWorld()), vector2.clone().normalize(), d);
         return rayTraceResult != null && rayTraceResult.getHitBlock() != null
            ? new BlockRayTraceResult(rayTraceResult.getHitPosition(), rayTraceResult.getHitBlock().getLocation().toVector().toBlockVector(), rayTraceResult.getHitBlockFace())
            : new BlockRayTraceResult();
      }

      Vector vector3 = vector.clone().add(vector2.clone().normalize().multiply(d));

      try {
         Object obj = (Object)WORLD_GET_HANDLE.invoke((World)entity.getWorld());
         Object x = (Object)VEC3D_CONSTRUCTOR.invoke((double)vector.getX(), (double)vector.getY(), (double)vector.getZ());
         Object x2 = (Object)VEC3D_CONSTRUCTOR.invoke((double)vector3.getX(), (double)vector3.getY(), (double)vector3.getZ());
         Object obj2 = (Object)WORLD_RAY_TRACE.invoke((Object)obj, (Object)x, (Object)x2);
         if (obj2 == null) {
            return new BlockRayTraceResult();
         }

         Object obj3 = (Object)HIT_GET_POS.invoke((Object)obj2);
         Object obj4 = (Object)HIT_GET_BLOCK_POS.invoke((Object)obj2);
         Vector vector4 = new Vector(
            (double)VEC_X.invoke((Object)obj3),
            (double)VEC_Y.invoke((Object)obj3),
            (double)VEC_Z.invoke((Object)obj3)
         );
         BlockVector blockVec = new BlockVector(
            (int)POS_X.invoke((Object)obj4), (int)POS_Y.invoke((Object)obj4), (int)POS_Z.invoke((Object)obj4)
         );
         BlockFace face = (BlockFace)NOTCH_TO_BLOCK_FACE.invoke((Object)(Object)HIT_GET_DIRECTION.invoke((Object)obj2));
         return new BlockRayTraceResult(vector4, blockVec, face);
      } catch (Throwable ex) {
         throw new RuntimeException(ex);
      }
   }

   static {
      try {
         if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)) {
            WORLD_GET_HANDLE = null;
            VEC3D_CONSTRUCTOR = null;
            HIT_GET_BLOCK_POS = null;
            HIT_GET_DIRECTION = null;
            NOTCH_TO_BLOCK_FACE = null;
            HIT_GET_POS = null;
            VEC_X = null;
            VEC_Y = null;
            VEC_Z = null;
            POS_X = null;
            POS_Y = null;
            POS_Z = null;
            WORLD_RAY_TRACE = null;
         } else {
            Class classVal = ReflectionUtils.getNmsVec3DClass();
            Class classVal2;
            if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_14)) {
               String str = "MovingObjectPositionBlock";
               classVal2 = ReflectionUtils.getNmsClass(str);
            } else {
               String str2 = "MovingObjectPosition";
               classVal2 = ReflectionUtils.getNmsClass(str2);
            }

            Class classVal3 = classVal2;
            Class classVal4 = ReflectionUtils.getNmsBlockPositionClass();
            String str3 = "EnumDirection";
            Class classVal5 = ReflectionUtils.getNmsClass(str3);
            Class classVal6 = ReflectionUtils.getCraftBlockClass();
            Lookup lookup = ReflectionAccess.LOOKUP;
            Class classVal7 = ReflectionUtils.getCraftWorldClass();
            Class[] classValArray = new Class[0];
            WORLD_GET_HANDLE = lookup.unreflect(
               (Method)ReflectionAccess.makeAccessible(classVal7.getMethod("getHandle", classValArray))
            );
            VEC3D_CONSTRUCTOR = ReflectionAccess.LOOKUP.findConstructor(classVal, MethodType.methodType(void.class, double.class, double.class, double.class));
            HIT_GET_POS = ReflectionAccess.LOOKUP.findGetter(classVal3, "pos", classVal);
            HIT_GET_BLOCK_POS = ReflectionAccess.LOOKUP
               .unreflect(Arrays.stream(classVal3.getMethods()).filter(m -> m.getReturnType().equals(classVal4)).findFirst().orElseThrow(NoSuchMethodException::new));
            MethodHandle handle;
            if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_14)) {
               Lookup lookup2 = ReflectionAccess.LOOKUP;
               MethodType methodType = MethodType.methodType(classVal5);
               handle = lookup2.findVirtual(classVal3, "getDirection", methodType);
            } else {
               handle = ReflectionAccess.LOOKUP.findGetter(classVal3, "direction", classVal5);
            }

            HIT_GET_DIRECTION = handle;
            Lookup lookup3 = ReflectionAccess.LOOKUP;
            MethodType methodType2 = MethodType.methodType(BlockFace.class, classVal5);
            NOTCH_TO_BLOCK_FACE = lookup3.findStatic(classVal6, "notchToBlockFace", methodType2);
            if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_9)) {
               VEC_X = ReflectionAccess.LOOKUP.findGetter(ReflectionUtils.getNmsVec3DClass(), "x", double.class);
               VEC_Y = ReflectionAccess.LOOKUP.findGetter(ReflectionUtils.getNmsVec3DClass(), "y", double.class);
               VEC_Z = ReflectionAccess.LOOKUP.findGetter(ReflectionUtils.getNmsVec3DClass(), "z", double.class);
            } else {
               VEC_X = ReflectionAccess.LOOKUP.findGetter(ReflectionUtils.getNmsVec3DClass(), "a", double.class);
               VEC_Y = ReflectionAccess.LOOKUP.findGetter(ReflectionUtils.getNmsVec3DClass(), "b", double.class);
               VEC_Z = ReflectionAccess.LOOKUP.findGetter(ReflectionUtils.getNmsVec3DClass(), "c", double.class);
            }

            Lookup lookup4 = ReflectionAccess.LOOKUP;
            Class[] classValArray2 = new Class[0];
            POS_X = lookup4.unreflect(classVal4.getMethod("getX", classValArray2));
            Lookup lookup5 = ReflectionAccess.LOOKUP;
            Class[] classValArray3 = new Class[0];
            POS_Y = lookup5.unreflect(classVal4.getMethod("getY", classValArray3));
            Lookup lookup6 = ReflectionAccess.LOOKUP;
            Class[] classValArray4 = new Class[0];
            POS_Z = lookup6.unreflect(classVal4.getMethod("getZ", classValArray4));
            Lookup lookup7 = ReflectionAccess.LOOKUP;
            classVal7 = ReflectionUtils.getNmsWorldClass();
            MethodType methodType3 = MethodType.methodType(classVal3, classVal, classVal);
            WORLD_RAY_TRACE = lookup7.findVirtual(classVal7, "rayTrace", methodType3);
         }
      } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException ex) {
         throw new RuntimeException(ex);
      }
   }
}
