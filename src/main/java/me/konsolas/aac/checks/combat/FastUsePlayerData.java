package me.konsolas.aac.checks.combat;


import me.konsolas.aac.checks.CheckPlayerData;
public class FastUsePlayerData extends CheckPlayerData {
   public long lastInteractMillis;

   private FastUsePlayerData() {
   }

   public FastUsePlayerData(FastUseStateAccess fastUseStateAccess) {
      this();
   }
}
