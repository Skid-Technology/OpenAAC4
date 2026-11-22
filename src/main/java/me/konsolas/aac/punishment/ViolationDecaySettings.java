package me.konsolas.aac.punishment;

public class ViolationDecaySettings {
   private final long interval;
   private final long delay;
   private final int amount;

   public ViolationDecaySettings(long ticks, long ticks2, int i) {
      this.interval = ticks;
      this.delay = ticks2;
      this.amount = i;
   }

   public static long getInterval(ViolationDecaySettings violationDecaySettings) {
      return violationDecaySettings.interval;
   }

   public static long getDelay(ViolationDecaySettings violationDecaySettings) {
      return violationDecaySettings.delay;
   }

   public static int getAmount(ViolationDecaySettings violationDecaySettings) {
      return violationDecaySettings.amount;
   }
}
