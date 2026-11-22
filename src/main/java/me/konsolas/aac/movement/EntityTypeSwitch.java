package me.konsolas.aac.movement;

import org.bukkit.entity.EntityType;

public class EntityTypeSwitch {
   public static final int[] ordinalSwitch = new int[EntityType.values().length];

   static {
      try {
         ordinalSwitch[EntityType.PLAYER.ordinal()] = 1;
      } catch (NoSuchFieldError ex) {
      }

      try {
         ordinalSwitch[EntityType.PIG.ordinal()] = 2;
      } catch (NoSuchFieldError ex2) {
      }

      try {
         ordinalSwitch[EntityType.HORSE.ordinal()] = 3;
      } catch (NoSuchFieldError ex3) {
      }

      try {
         ordinalSwitch[EntityType.SKELETON_HORSE.ordinal()] = 4;
      } catch (NoSuchFieldError ex4) {
      }

      try {
         ordinalSwitch[EntityType.DONKEY.ordinal()] = 5;
      } catch (NoSuchFieldError ex5) {
      }

      try {
         ordinalSwitch[EntityType.MULE.ordinal()] = 6;
      } catch (NoSuchFieldError ex6) {
      }

      try {
         ordinalSwitch[EntityType.LLAMA.ordinal()] = 7;
      } catch (NoSuchFieldError ex7) {
      }
   }
}
