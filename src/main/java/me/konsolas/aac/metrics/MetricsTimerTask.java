package me.konsolas.aac.metrics;


import java.util.TimerTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class MetricsTimerTask extends TimerTask {
   public final java.util.Timer timerValue;
   public final MetricsLite metricsLiteValue;

   public MetricsTimerTask(MetricsLite metricsLite, java.util.Timer timer) {
      this.metricsLiteValue = metricsLite;
      this.timerValue = timer;
   }

   @Override
   public void run() {
      if (!MetricsLite.getPlugin(this.metricsLiteValue).isEnabled()) {
         this.timerValue.cancel();
      } else {
         BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
         Plugin plugin = MetricsLite.getPlugin(this.metricsLiteValue);
         Runnable runnable = () -> MetricsLite.submitData(this.metricsLiteValue);
         Plugin plugin2 = plugin;
         BukkitScheduler bukkitScheduler2 = bukkitScheduler;
         bukkitScheduler2.runTask(plugin2, runnable);
      }
   }
}
