package me.konsolas.aac.movement;

import java.util.Objects;
import org.bukkit.util.Vector;

public class MotionPrediction {
   private final MovementInput input;
   private final MovementControls controls;
   private final MotionVectorState vectorState;
   private final VelocityState velocityState;
   private final int depth;

   public MotionPrediction(MovementInput movementInput, VelocityState velocityState, MovementControls movementControls, MotionVectorState motionVectorState) {
      this(movementInput, velocityState, movementControls, motionVectorState, 0);
   }

   private MotionPrediction(MovementInput movementInput, VelocityState velocityState, MovementControls movementControls, MotionVectorState motionVectorState, int i) {
      this.input = movementInput;
      this.velocityState = velocityState;
      this.controls = movementControls;
      this.vectorState = motionVectorState;
      this.depth = i;
   }

   public MovementControls getControls() {
      return this.controls;
   }

   public MotionVectorState getVectorState() {
      return this.vectorState;
   }

   public VelocityState getVelocityState() {
      return this.velocityState;
   }

   public int getDepth() {
      return this.depth;
   }

   public MotionPrediction chainOffset(Vector vector) {
      return new MotionPrediction(
         this.input,
         this.velocityState,
         this.controls,
         new MotionVectorState(
            this.vectorState.getPositionDelta().clone().add(vector),
            this.vectorState.getVelocity(),
            this.vectorState.isOnGround(),
            this.vectorState.hasHorizontalCollision()
         ),
         this.depth + 1
      );
   }

   public MotionPrediction withActualMovement(Vector vector) {
      Vector vector2 = vector.clone();
      return new MotionPrediction(
         this.input,
         this.velocityState,
         this.controls,
         new MotionVectorState(vector2, vector2, this.vectorState.isOnGround(), this.vectorState.hasHorizontalCollision()),
         this.depth
      );
   }

   public MotionPrediction withVelocity(Vector vector) {
      return new MotionPrediction(
         this.input,
         this.velocityState,
         this.controls,
         new MotionVectorState(this.vectorState.getPositionDelta(), vector, this.vectorState.isOnGround(), this.vectorState.hasHorizontalCollision()),
         this.depth
      );
   }

   public MotionPrediction withOnGround() {
      return new MotionPrediction(
         this.input,
         this.velocityState,
         this.controls,
         new MotionVectorState(this.vectorState.getPositionDelta(), this.vectorState.getVelocity(), true, this.vectorState.hasHorizontalCollision()),
         this.depth
      );
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         MotionPrediction motionPrediction = (MotionPrediction)obj;
         return Objects.equals(this.controls, motionPrediction.controls)
            && Objects.equals(this.vectorState, motionPrediction.vectorState)
            && Objects.equals(this.velocityState, motionPrediction.velocityState);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.controls, this.vectorState, this.velocityState);
   }

   public static boolean unusedPredictionFlag() {
      boolean flag = false;
      return !flag;
   }

   static {
      if (!unusedPredictionFlag()) {
      }
   }
}
