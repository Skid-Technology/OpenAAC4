package me.konsolas.aac.checks.combat;


import me.konsolas.aac.checks.CheckPlayerData;
import me.konsolas.aac.util.TimedThresholdBuffer;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class KillauraPlayerData extends CheckPlayerData {
   private Set<CombatCheckModule> modules;
   private long lastAttackTick = 0L;
   private TimedThresholdBuffer moveBuffer = new TimedThresholdBuffer(20L, 10);
   public BooleanSupplier combatReadySupplier;

   public static Set<CombatCheckModule> getModules(KillauraPlayerData data) {
      return data.modules;
   }

   public static Set<CombatCheckModule> setModules(KillauraPlayerData data, Set<CombatCheckModule> set) {
      return data.modules = set;
   }

   public static long setLastAttackTick(KillauraPlayerData data, long ticks) {
      return data.lastAttackTick = ticks;
   }

   public static long getLastAttackTick(KillauraPlayerData data) {
      return data.lastAttackTick;
   }

   public static TimedThresholdBuffer getMoveBuffer(KillauraPlayerData data) {
      return data.moveBuffer;
   }
}
