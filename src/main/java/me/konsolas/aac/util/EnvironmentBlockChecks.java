package me.konsolas.aac.util;

import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;

public class EnvironmentBlockChecks {
   private static final Set STAIR_MATERIALS = EnumSet.noneOf(Material.class);

   public static boolean isInCobweb(RelativeBlockAccess relativeBlockAccess) {
      return relativeBlockAccess.getSubtractedMaterial(0.0, 0.0, 0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getSubtractedMaterial(0.3, 0.0, 0.0) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getSubtractedMaterial(0.0, 0.0, -0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getSubtractedMaterial(-0.3, 0.0, 0.0) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getSubtractedMaterial(-0.3, 0.0, -0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getSubtractedMaterial(-0.3, 0.0, 0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getSubtractedMaterial(0.3, 0.0, -0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getSubtractedMaterial(0.3, 0.0, 0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(0.0, 0.8, 0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(0.3, 0.8, 0.0) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(0.0, 0.8, -0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(-0.3, 0.8, 0.0) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(-0.3, 0.8, -0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(-0.3, 0.8, 0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(0.3, 0.8, -0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(0.3, 0.8, 0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(0.0, 1.65, 0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(0.3, 1.65, 0.0) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(0.0, 1.65, -0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(-0.3, 1.65, 0.0) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(-0.3, 1.65, -0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(-0.3, 1.65, 0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(0.3, 1.65, -0.3) == MaterialGroup.COBWEB.getMaterial()
         || relativeBlockAccess.getRelativeMaterial(0.3, 1.65, 0.3) == MaterialGroup.COBWEB.getMaterial();
   }

   private static boolean isClimbable(Material material) {
      return material == Material.LADDER || material == Material.VINE;
   }

   public static boolean isOnClimbable(RelativeBlockAccess relativeBlockAccess) {
      return isClimbable(relativeBlockAccess.getSubtractedMaterial(0.0, 0.0, 0.3))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(0.3, 0.0, 0.0))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(0.0, 0.0, -0.3))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(-0.3, 0.0, 0.0))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(-0.3, 0.0, -0.3))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(-0.3, 0.0, 0.3))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(0.3, 0.0, -0.3))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(0.3, 0.0, 0.3))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(0.0, 1.0, 0.3))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(0.3, 1.0, 0.0))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(0.0, 1.0, -0.3))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(-0.3, 1.0, 0.0))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(-0.3, 1.0, -0.3))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(-0.3, 1.0, 0.3))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(0.3, 1.0, -0.3))
         || isClimbable(relativeBlockAccess.getSubtractedMaterial(0.3, 1.0, 0.3));
   }

   public static boolean isInLiquid(RelativeBlockAccess relativeBlockAccess) {
      return relativeBlockAccess.getRelativeBlock(0.0, 0.0, 0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.3, 0.0, 0.0).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.0, 0.0, -0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(-0.3, 0.0, 0.0).isLiquid()
         || relativeBlockAccess.getRelativeBlock(-0.3, 0.0, -0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(-0.3, 0.0, 0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.3, 0.0, -0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.3, 0.0, 0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.0, 0.8, 0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.3, 0.8, 0.0).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.0, 0.8, -0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(-0.3, 0.8, 0.0).isLiquid()
         || relativeBlockAccess.getRelativeBlock(-0.3, 0.8, -0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(-0.3, 0.8, 0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.3, 0.8, -0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.3, 0.8, 0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.0, 1.45, 0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.3, 1.45, 0.0).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.0, 1.45, -0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(-0.3, 1.45, 0.0).isLiquid()
         || relativeBlockAccess.getRelativeBlock(-0.3, 1.45, -0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(-0.3, 1.45, 0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.3, 1.45, -0.3).isLiquid()
         || relativeBlockAccess.getRelativeBlock(0.3, 1.45, 0.3).isLiquid();
   }

   static {

      for (Material material : Material.values()) {
         if (material.name().endsWith("STAIRS") && !material.name().startsWith("LEGACY")) {
            STAIR_MATERIALS.add(material);
         }
      }
   }
}
