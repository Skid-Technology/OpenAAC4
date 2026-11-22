package me.konsolas.aac.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class RelativeBlockAccess {
   private final double baseX;
   private final double baseY;
   private final double baseZ;
   private final World world;

   public RelativeBlockAccess(Location location) {
      this.baseX = location.getX();
      this.baseY = location.getY();
      this.baseZ = location.getZ();
      this.world = location.getWorld();
   }

   private static int floorCoord(double d) {
      return (int)(d + 3.0E7) - 30000000;
   }

   public Block getRelativeBlock(double d, double d2, double d3) {
      return this.world.getBlockAt(floorCoord(this.baseX + d), floorCoord(this.baseY + d2), floorCoord(this.baseZ + d3));
   }

   public Block getSubtractedBlock(double d, double d2, double d3) {
      return this.world.getBlockAt(floorCoord(this.baseX - d), floorCoord(this.baseY - d2), floorCoord(this.baseZ - d3));
   }

   public Material getRelativeMaterial(double d, double d2, double d3) {
      return this.getRelativeBlock(d, d2, d3).getType();
   }

   public Material getSubtractedMaterial(double d, double d2, double d3) {
      return this.getSubtractedBlock(d, d2, d3).getType();
   }

   public static boolean unusedFlag() {
      boolean flag = false;
      return !flag;
   }

   static {
      if (unusedFlag()) {
      }
   }
}
