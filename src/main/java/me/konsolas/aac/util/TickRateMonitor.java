package me.konsolas.aac.util;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TickRateMonitor extends BukkitRunnable {
   private static TickRateMonitor instance;
   private final int sampleCount = 40;
   private long lastTickMillis = System.currentTimeMillis();
   private final Deque<Long> tickDurationsMillis = new ArrayDeque<>(Collections.nCopies(40, 50L));

   private TickRateMonitor(Plugin plugin) {
      this.runTaskTimer(plugin, 1L, 1L);
   }

   public static double getTicksPerSecond() {
      try {
         double d = 1000.0 / instance.getAverageTickMillis();
         return d > 20.0 ? 20.0 : d;
      } catch (Exception ex) {
         return 20.0;
      }
   }

   public static void start(Plugin plugin) {
      instance = new TickRateMonitor(plugin);
   }

   public void run() {
      long now = System.currentTimeMillis();
      long ticks = now - this.lastTickMillis;
      this.lastTickMillis = now;
      this.tickDurationsMillis.removeFirst();
      this.tickDurationsMillis.addLast(ticks);
   }

   public double getAverageTickMillis() {
      int value = 0;

      for (long ticks : this.tickDurationsMillis) {
         value = (int)(value + ticks);
      }

      return value / 40.0;
   }
}
