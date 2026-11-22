package me.konsolas.aac.util;

import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;

public class PlayerDigTypeSwitch {
   public static final int[] ordinalSwitch = new int[PlayerDigType.values().length];

   static {
      try {
         ordinalSwitch[PlayerDigType.START_DESTROY_BLOCK.ordinal()] = 1;
      } catch (NoSuchFieldError ex) {
      }

      try {
         ordinalSwitch[PlayerDigType.STOP_DESTROY_BLOCK.ordinal()] = 2;
      } catch (NoSuchFieldError ex2) {
      }

      try {
         ordinalSwitch[PlayerDigType.ABORT_DESTROY_BLOCK.ordinal()] = 3;
      } catch (NoSuchFieldError ex3) {
      }
   }
}
