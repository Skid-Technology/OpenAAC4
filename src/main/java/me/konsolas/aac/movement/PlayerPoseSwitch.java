package me.konsolas.aac.movement;

public class PlayerPoseSwitch {
   public static final int[] ordinalSwitch = new int[PlayerPose.values().length];

   static {
      try {
         ordinalSwitch[PlayerPose.STANDING.ordinal()] = 1;
      } catch (NoSuchFieldError ex) {
      }

      try {
         ordinalSwitch[PlayerPose.SNEAKING.ordinal()] = 2;
      } catch (NoSuchFieldError ex2) {
      }

      try {
         ordinalSwitch[PlayerPose.FALL_FLYING.ordinal()] = 3;
      } catch (NoSuchFieldError ex3) {
      }

      try {
         ordinalSwitch[PlayerPose.SWIMMING.ordinal()] = 4;
      } catch (NoSuchFieldError ex4) {
      }
   }
}
