package me.konsolas.aac.movement;

import org.bukkit.util.Vector;

public class VelocityState {
   private final Vector velocity;
   private final boolean applied;

   public VelocityState(Vector vector, boolean flag) {
      this.velocity = vector;
      this.applied = flag;
   }

   public Vector getVelocity() {
      return this.velocity.clone();
   }

   public boolean isApplied() {
      return this.applied;
   }
}
