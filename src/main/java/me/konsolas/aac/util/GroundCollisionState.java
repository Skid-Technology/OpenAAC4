package me.konsolas.aac.util;

public enum GroundCollisionState {
   TRUE_NORMAL(true, false, false),
   TRUE_AIRSTEP(true, true, false),
   FALSE_NORMAL(false, false, false),
   FALSE_AIRSTEP(false, true, false),
   TRUE_NORMAL_COLLIDED(true, false, true),
   TRUE_AIRSTEP_COLLIDED(true, true, true),
   FALSE_NORMAL_COLLIDED(false, false, true),
   FALSE_AIRSTEP_COLLIDED(false, true, true);

   private final boolean onGround;
   private final boolean airStep;
   private final boolean collided;

   private GroundCollisionState(boolean flag, boolean flag2, boolean flag3) {
      this.onGround = flag;
      this.airStep = flag2;
      this.collided = flag3;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public boolean isAirStep() {
      return this.airStep;
   }

   public static GroundCollisionState of(boolean flag, boolean flag2, boolean flag3) {
      if (flag) {
         if (flag2) {
            return flag3 ? TRUE_AIRSTEP_COLLIDED : TRUE_AIRSTEP;
         } else {
            return flag3 ? TRUE_NORMAL_COLLIDED : TRUE_NORMAL;
         }
      } else if (flag2) {
         return flag3 ? FALSE_AIRSTEP_COLLIDED : FALSE_AIRSTEP;
      } else {
         return flag3 ? FALSE_NORMAL_COLLIDED : FALSE_NORMAL;
      }
   }

   public boolean hasCollided() {
      return this.collided;
   }

   @Override
   public String toString() {
      return (this.isOnGround() ? "+" : "-") + (this.isAirStep() ? "a" : "") + (this.hasCollided() ? "!" : "");
   }


}
