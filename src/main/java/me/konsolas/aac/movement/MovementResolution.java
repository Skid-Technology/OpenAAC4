package me.konsolas.aac.movement;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class MovementResolution {
   private final Location location;
   private final double scale;
   private final Vector movement;

   public MovementResolution(Location location, double d, Vector vector) {
      this.location = location;
      this.scale = d;
      this.movement = vector;
   }

   public static MovementResolution setback() {
      return new MovementResolution(null, 1.0, null);
   }

   public Location getLocation() {
      return this.location;
   }

   public double getScale() {
      return this.scale;
   }

   public Vector getMovement() {
      return this.movement;
   }
}
