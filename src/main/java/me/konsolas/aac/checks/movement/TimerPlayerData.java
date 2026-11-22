package me.konsolas.aac.checks.movement;


import me.konsolas.aac.checks.CheckPlayerData;
public class TimerPlayerData extends CheckPlayerData {
   private final Object balanceLock = new Object();
   public double balance;
   public long lastPacketNanos;

   public TimerPlayerData() {
   }

   public static Object getBalanceLock(TimerPlayerData data) {
      return data.balanceLock;
   }

}
