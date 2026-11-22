package me.konsolas.aac;


import me.konsolas.aac.api.internal.AacApiImplementation;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.command.AacCommandExecutor;
import me.konsolas.aac.compat.AacPlaceholderExpansion;
import me.konsolas.aac.compat.PlayerConnectionInjector;
import me.konsolas.aac.metrics.MetricsLite;
import me.konsolas.aac.punishment.BanManager;
import me.konsolas.aac.punishment.PunishmentManager;
import me.konsolas.aac.punishment.ViolationLogger;
import me.konsolas.aac.util.TickRateMonitor;
import me.konsolas.aac.api.AACAPI;
import me.konsolas.aac.api.HackType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class AAC extends JavaPlugin {
   private static AAC instance;
   private boolean placeholderApiEnabled;
   public ViolationLogger violationLogger;
   public CheckManager checkManager;
   public PunishmentManager punishmentManager;
   public AacCommandExecutor commandExecutor;
   public BanManager banManager;
   public static AACAPI api;

   public AAC() {
      instance = this;
   }

   public void onEnable() {
      if (!this.getName().equals("AAC")) {
         throw new IllegalStateException("Plugin file no longer valid");
      }

      this.loadConfiguration();
      PluginManager pluginManager = this.getServer().getPluginManager();
      this.placeholderApiEnabled = pluginManager.getPlugin("PlaceholderAPI") != null;
      if (this.placeholderApiEnabled) {
         new AacPlaceholderExpansion(this).register();
         this.getLogger().info("Hooked into PlaceholderAPI");
      }

      instance.checkManager = new CheckManager(instance);
      boolean logConsole = this.getConfig().getBoolean("log_console");
      instance.violationLogger = new ViolationLogger(instance, logConsole);
      instance.punishmentManager = new PunishmentManager(instance);
      instance.commandExecutor = new AacCommandExecutor(instance);
      instance.banManager = new BanManager(instance);
      if (this.getConfig().getBoolean("compress_old_log_files")) {
         this.violationLogger.compressOldLogs();
      }

      this.violationLogger.openLog();
      this.checkManager.start();
      this.punishmentManager.loadThresholds();
      TickRateMonitor.start(this);

      for (HackType hackType : HackType.values()) {
         if (this.getConfig().getBoolean(hackType.getName().toLowerCase() + ".enabled")) {
            this.checkManager.enableCheck(hackType);
         } else {
            this.getViolationLogger().logSystem(hackType + ": disabled in config.yml");
         }
      }

      this.getLogger().info("Enabled: " + this.checkManager.getEnabledCheckTypes());
      this.getCommand("aac").setExecutor(this.commandExecutor);
      this.getCommand("aac").setTabCompleter(this.commandExecutor);
      new MetricsLite(this);
      api = new AacApiImplementation(this);
   }

   private void loadConfiguration() {
      this.saveDefaultConfig();
      this.reloadConfig();
   }

   public void onDisable() {
      if (this.checkManager != null) {
         this.checkManager.shutdown();
      }

      if (this.punishmentManager != null) {
         this.punishmentManager.shutdown();
      }

      if (this.violationLogger != null) {
         this.violationLogger.closeLog();
      }

      if (this.banManager != null) {
         this.banManager.close();
      }
   }

   public boolean isPlaceholderApiEnabled() {
      return this.placeholderApiEnabled;
   }

   public void reloadPlugin() {
      this.onDisable();
      this.onEnable();
   }

   public ViolationLogger getViolationLogger() {
      return this.violationLogger;
   }

   public CheckManager getCheckManager() {
      return this.checkManager;
   }

   public static AAC getInstance() {
      return instance;
   }

   public static void runOnMainThread(Runnable runnable) {
      BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
      Runnable runnable2 = runnable;
      AAC plugin = instance;
      BukkitScheduler bukkitScheduler2 = bukkitScheduler;
      bukkitScheduler2.scheduleSyncDelayedTask(plugin, runnable2);
   }

   static {
      new PlayerConnectionInjector().run();
   }
}
