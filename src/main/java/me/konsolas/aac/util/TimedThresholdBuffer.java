package me.konsolas.aac.util;

public class TimedThresholdBuffer {
   private long lastTick;
   private int count;
   private final long windowTicks;
   private final long threshold;

   public TimedThresholdBuffer(long ticks, int i) {
      this.windowTicks = ticks;
      this.threshold = i;
   }

   public boolean record(long ticks, int i) {
      if (this.lastTick + this.windowTicks >= ticks) {
         this.lastTick = ticks;
         return (this.count += i) >= this.threshold;
      } else {
         this.count = 1;
         this.lastTick = ticks;
         return false;
      }
   }

   public boolean record(long ticks) {
      return this.record(ticks, 1);
   }

   public void decrement(int i) {
      this.count = Math.max(this.count - i, 0);
   }

   public int getCount() {
      return this.count;
   }

   public void reset() {
      this.lastTick = 0L;
      this.count = 0;
   }
}
