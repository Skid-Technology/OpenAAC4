package me.konsolas.aac.compat;

public class ProtocolVersionSwitch {
   public static final int[] ordinalSwitch = new int[ProtocolVersion.values().length];

   static {
      try {
         ordinalSwitch[ProtocolVersion.V1_8.ordinal()] = 1;
      } catch (NoSuchFieldError ex) {
      }

      try {
         ordinalSwitch[ProtocolVersion.V1_9.ordinal()] = 2;
      } catch (NoSuchFieldError ex2) {
      }

      try {
         ordinalSwitch[ProtocolVersion.V1_10.ordinal()] = 3;
      } catch (NoSuchFieldError ex3) {
      }

      try {
         ordinalSwitch[ProtocolVersion.V1_11.ordinal()] = 4;
      } catch (NoSuchFieldError ex4) {
      }

      try {
         ordinalSwitch[ProtocolVersion.V1_12.ordinal()] = 5;
      } catch (NoSuchFieldError ex5) {
      }

      try {
         ordinalSwitch[ProtocolVersion.V1_13.ordinal()] = 6;
      } catch (NoSuchFieldError ex6) {
      }

      try {
         ordinalSwitch[ProtocolVersion.V1_14.ordinal()] = 7;
      } catch (NoSuchFieldError ex7) {
      }

      try {
         ordinalSwitch[ProtocolVersion.V1_15.ordinal()] = 8;
      } catch (NoSuchFieldError ex8) {
      }
   }
}
