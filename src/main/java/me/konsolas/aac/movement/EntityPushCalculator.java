package me.konsolas.aac.movement;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EntityPushCalculator {
   public static HorizontalVector calculatePushVector(Player player) {
      HorizontalVector horizontalVector = HorizontalVector.ZERO;

      for (Entity entity : player.getNearbyEntities(1.0, 1.0, 1.0)) {
         if (!entity.equals(player.getVehicle())) {
            double x = player.getLocation().getX() - entity.getLocation().getX();
            double z = player.getLocation().getZ() - entity.getLocation().getZ();
            double d = Math.max(Math.abs(x), Math.abs(z));
            if (d > 0.01F) {
               d = Math.sqrt(d);
               x /= d;
               z /= d;
               double d2 = Math.min(1.0, 1.0 / d);
               x *= d2;
               z *= d2;
               x *= 0.05;
               z *= 0.05;
               x *= 1.0;
               z *= 1.0;
               horizontalVector = horizontalVector.add(new HorizontalVector(x, z));
            }
         }
      }

      return horizontalVector;
   }

   public static int unusedPushMarker() {
      boolean flag = false;
      return !flag ? 16 : 0;
   }

   static {
      if (unusedPushMarker() != 0) {
      }
   }
}
