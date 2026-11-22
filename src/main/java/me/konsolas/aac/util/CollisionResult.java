package me.konsolas.aac.util;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.World;
import org.bukkit.block.Block;

public class CollisionResult {
   private final World world;
   private final BoundingBox boundingBox;
   private final List<BoundingBox> collisionBoxes;
   private final double distance;

   public CollisionResult(World world, BoundingBox box, List<BoundingBox> list, double d) {
      this.world = world;
      this.boundingBox = box;
      this.collisionBoxes = list;
      this.distance = d;
   }

   public List<BoundingBox> getCollisionBoxes() {
      return this.collisionBoxes;
   }

   public double getDistance() {
      return this.distance;
   }

   @Override
   public String toString() {
      return this.getCollisionBoxes().stream().map(bb -> {
         Block x = this.world.getBlockAt((int)bb.getMin().getX(), (int)bb.getMin().getY(), (int)bb.getMin().getZ());
         return x.getType() + "@" + x.getLocation();
      }).collect(Collectors.joining(" "));
   }
}
