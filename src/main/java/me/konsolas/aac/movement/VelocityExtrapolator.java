package me.konsolas.aac.movement;


import me.konsolas.aac.util.CollisionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class VelocityExtrapolator {
   public static Location predictLocation(Player player, Vector vector, boolean flag, Location location, double d) {
      Vector vector2 = predictDisplacement(vector, d);
      if (flag) {
         vector2.setX(0.8 * vector2.getX());
         vector2.setZ(0.8 * vector2.getZ());
      }

      Vector vector3 = vector2;
      Location location2 = location;
      Player player2 = player;
      CollisionUtils.getCollidingBoxes(player2, location2, vector3);
      return location.clone().add(vector2);
   }

   public static Vector predictVelocity(Vector vector, double d) {
      double y = (-98.0 + Math.pow(0.98, d) * (98.0 + 25.0 * vector.getY())) / 25.0;
      double x = Math.pow(0.98, d) * vector.getX();
      double z = Math.pow(0.98, d) * vector.getZ();
      return new Vector(x, y, z);
   }

   private static Vector predictDisplacement(Vector vector, double d) {
      double y = d * (-98.0 + Math.pow(0.98, d) * (98.0 + 25.0 * vector.getY())) / 25.0;
      double x = Math.pow(0.98, d) * d * vector.getX();
      double z = Math.pow(0.98, d) * d * vector.getZ();
      return new Vector(x, y, z);
   }
}
