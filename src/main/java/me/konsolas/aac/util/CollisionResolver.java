package me.konsolas.aac.util;


import me.konsolas.aac.compat.ProtocolVersion;
import me.konsolas.aac.movement.MotionPrediction;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class CollisionResolver {
   public static List<BoundingBox> refineBlockBoxes(ProtocolVersion protocolVersion, World world, List<BoundingBox> list) {
      ArrayList<BoundingBox> list2 = new ArrayList<>(list.size());

      for (BoundingBox box : list) {
         Block block = box.getMin().toLocation(world).getBlock();
         if (block.getType() == Material.LADDER) {
            BoundingBox box2 = box;
            ProtocolVersion protocolVersion2 = protocolVersion;
            list2.add(shrinkLadderBox(protocolVersion2, box2));
         } else if (block.getType() == MaterialGroup.LILY_PAD.getMaterial()) {
            list2.add(shrinkLilyPadBox(protocolVersion, box));
         } else if (block.getType() == MaterialGroup.FARMLAND.getMaterial()) {
            list2.add(shrinkFarmlandBox(protocolVersion, box));
         } else if (MaterialGroup.CARPET.contains(block.getType())) {
            list2.add(shrinkCarpetBox(protocolVersion, box));
         } else {
            list2.add(box);
         }
      }

      return list2;
   }

   private static BoundingBox shrinkLadderBox(ProtocolVersion protocolVersion, BoundingBox box) {
      MotionPrediction.unusedPredictionFlag();
      double d;
      if (protocolVersion.isAtLeast(ProtocolVersion.V1_9)) {
         d = 0.1875;
      } else {
         d = 0.125;
      }

      if (isIntegerCoordinate(box.getMin().getX())) {
         return box.withMinX(box.getMax().getX() - d);
      } else if (isIntegerCoordinate(box.getMin().getZ())) {
         return box.withMinZ(box.getMax().getZ() - d);
      } else if (isIntegerCoordinate(box.getMax().getX())) {
         return box.withMaxX(box.getMin().getX() + d);
      } else if (isIntegerCoordinate(box.getMax().getZ())) {
         return box.withMaxZ(box.getMin().getZ() + d);
      } else {
         throw new IllegalArgumentException("unrecognised ladder hitbox " + box);
      }
   }

   private static BoundingBox shrinkLilyPadBox(ProtocolVersion protocolVersion, BoundingBox box) {
      MotionPrediction.unusedPredictionFlag();
      double d;
      double d2;
      if (protocolVersion.isAtLeast(ProtocolVersion.V1_9)) {
         d = 0.09375;
         d2 = 0.0625;
      } else {
         d = 0.015625;
         d2 = 0.0;
      }

      return new BoundingBox(
         Math.floor(box.getMin().getX()) + d2,
         box.getMin().getY(),
         Math.floor(box.getMin().getZ()) + d2,
         Math.ceil(box.getMax().getX()) - d2,
         box.getMin().getY() + d,
         Math.ceil(box.getMax().getZ()) - d2
      );
   }

   private static BoundingBox shrinkFarmlandBox(ProtocolVersion protocolVersion, BoundingBox box) {
      MotionPrediction.unusedPredictionFlag();
      double d;
      if (protocolVersion.isAtLeast(ProtocolVersion.V1_9)) {
         d = 0.9375;
      } else {
         d = 1.0;
      }

      return box.withMaxY(box.getMin().getY() + d);
   }

   private static BoundingBox shrinkCarpetBox(ProtocolVersion protocolVersion, BoundingBox box) {
      double d = 0.0625;
      return box.withMaxY(box.getMin().getY() + d);
   }

   private static boolean isIntegerCoordinate(double d) {
      MotionPrediction.unusedPredictionFlag();
      return d != (int)d;
   }
}
