package me.konsolas.aac.checks.world;


import me.konsolas.aac.checks.CheckPlayerData;
public class FastPlacePlayerData extends CheckPlayerData {
   public long lastIntervalMillis;
   public long lastPlaceMillis;

   private FastPlacePlayerData() {
   }

   public FastPlacePlayerData(FastPlaceStateAccess fastPlaceStateAccess) {
      this();
   }
}
