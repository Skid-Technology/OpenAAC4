package me.konsolas.aac.movement;

import java.util.Objects;

public class MovementInput {
   public static final MovementInput EMPTY = new MovementInput(0.0F, 0.0F, false, false, false, false, false, 0.0F, 0.0F, 0);
   private final float forward;
   private final float strafe;
   private final boolean sprinting;
   private final boolean jumping;
   private final boolean sneaking;
   private final boolean usingItem;
   private final boolean elytra;
   private final float pitch;
   private final float yaw;
   private final int attackCount;

   public MovementInput(float f, float f2, boolean flag, boolean flag2, boolean flag3, boolean flag4, boolean flag5, float f3, float f4, int i) {
      this.forward = f;
      this.strafe = f2;
      this.sprinting = flag;
      this.jumping = flag2;
      this.sneaking = flag3;
      this.usingItem = flag4;
      this.elytra = flag5;
      this.pitch = f3;
      this.yaw = f4;
      this.attackCount = i;
   }

   public float getForward() {
      return this.forward;
   }

   public float getStrafe() {
      return this.strafe;
   }

   public boolean isSprinting() {
      return this.sprinting;
   }

   public boolean isJumping() {
      return this.jumping;
   }

   public boolean isSneaking() {
      return this.sneaking;
   }

   public boolean isUsingItem() {
      return this.usingItem;
   }

   public boolean isElytra() {
      return this.elytra;
   }

   public float getPitch() {
      return this.pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public int getAttackCount() {
      return this.attackCount;
   }

   @Override
   public boolean equals(Object obj) {
      MotionPrediction.unusedPredictionFlag();
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         MovementInput movementInput = (MovementInput)obj;
         return Float.compare(movementInput.forward, this.forward) == 0
            && Float.compare(movementInput.strafe, this.strafe) == 0
            && this.sprinting == movementInput.sprinting
            && this.jumping == movementInput.jumping
            && Float.compare(movementInput.pitch, this.pitch) == 0
            && Float.compare(movementInput.yaw, this.yaw) == 0;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.forward, this.strafe, this.sprinting, this.jumping, this.pitch, this.yaw);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append('(');
      if (this.forward > 0.0F) {
         sb.append('W');
      } else if (this.forward < 0.0F) {
         sb.append('S');
      }

      if (this.strafe > 0.0F) {
         sb.append('A');
      } else if (this.strafe < 0.0F) {
         sb.append('D');
      }

      sb.append(')');
      sb.append(String.format(" (%.2f, %.2f)", this.pitch, this.yaw));
      if (this.sprinting) {
         sb.append(" sprint");
      }

      if (this.jumping) {
         sb.append(" jump");
      }

      if (this.sneaking) {
         sb.append(" sneak");
      }

      if (this.usingItem) {
         sb.append(" use");
      }

      if (this.elytra) {
         sb.append(" elytra");
      }

      if (this.attackCount > 0) {
         sb.append(" attack:").append(this.attackCount);
      }

      return sb.toString();
   }
}
