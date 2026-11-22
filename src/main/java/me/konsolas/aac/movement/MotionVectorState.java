package me.konsolas.aac.movement;

import java.util.Objects;
import org.bukkit.util.Vector;

public class MotionVectorState {
   private final Vector positionDelta;
   private final Vector velocity;
   private final boolean onGround;
   private final boolean horizontalCollision;

   public MotionVectorState(Vector vector, Vector vector2, boolean flag, boolean flag2) {
      this.positionDelta = vector;
      this.velocity = vector2;
      this.onGround = flag;
      this.horizontalCollision = flag2;
   }

   public Vector getPositionDelta() {
      return this.positionDelta;
   }

   public Vector getVelocity() {
      return this.velocity;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public boolean hasHorizontalCollision() {
      return this.horizontalCollision;
   }

   @Override
   public boolean equals(Object obj) {
      MotionPrediction.unusedPredictionFlag();
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         MotionVectorState motionVectorState = (MotionVectorState)obj;
         return this.onGround == motionVectorState.onGround
            && this.horizontalCollision == motionVectorState.horizontalCollision
            && Objects.equals(this.positionDelta, motionVectorState.positionDelta)
            && Objects.equals(this.velocity, motionVectorState.velocity);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.positionDelta, this.velocity, this.onGround, this.horizontalCollision);
   }
}
