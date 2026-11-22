package me.konsolas.aac.movement;


import me.konsolas.aac.compat.ProtocolVersion;
import me.konsolas.aac.util.BoundingBox;
import java.util.Collection;
import org.bukkit.util.Vector;

public enum PlayerPose {
   STANDING(),
   SNEAKING(),
   FALL_FLYING(),
   SWIMMING();

   private static final EntityDimensions STANDING_DIMENSIONS = new EntityDimensions(0.6F, 1.8F);
   private static final EntityDimensions LEGACY_SNEAKING_DIMENSIONS = new EntityDimensions(0.6F, 1.65F);
   private static final EntityDimensions SNEAKING_DIMENSIONS = new EntityDimensions(0.6F, 1.5);
   private static final EntityDimensions SWIMMING_DIMENSIONS = new EntityDimensions(0.6F, 0.6F);

   public EntityDimensions getDimensions(ProtocolVersion protocolVersion) {
      switch (PlayerPoseSwitch.ordinalSwitch[this.ordinal()]) {
         case 1:
            return STANDING_DIMENSIONS;
         case 2:
            if (protocolVersion.isAtLeast(ProtocolVersion.V1_14)) {
               return SNEAKING_DIMENSIONS;
            } else {
               if (protocolVersion.isAtLeast(ProtocolVersion.V1_9)) {
                  return LEGACY_SNEAKING_DIMENSIONS;
               }

               return STANDING_DIMENSIONS;
            }
         case 3:
         case 4:
            return SWIMMING_DIMENSIONS;
         default:
            throw new AssertionError();
      }
   }

   public boolean canFit(ProtocolVersion protocolVersion, Vector vector, Collection collection) {
      return new BoundingBox(vector, this.getDimensions(protocolVersion)).isDisjointWith(collection);
   }


}
