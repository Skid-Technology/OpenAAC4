package me.konsolas.aac.punishment;


import me.konsolas.aac.AAC;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class BanManager implements Closeable {
   private final AAC plugin;
   private final BanConfiguration banConfig;
   private final long expirationMillis;
   private final int banThreshold;
   private final String banCommand;
   private final Executor ioExecutor = Executors.newSingleThreadExecutor();
   private static final SimpleDateFormat BAN_LOG_DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

   public BanManager(AAC plugin) {
      this.plugin = plugin;
      this.banConfig = new BanConfiguration(plugin);
      this.banConfig.saveDefaultConfig();
      this.banConfig.reloadConfig();
      this.expirationMillis = TimeUnit.valueOf(this.banConfig.getConfig().getString("expiration_time_unit"))
         .toMillis(this.banConfig.getConfig().getLong("expiration_time"));
      this.banThreshold = this.banConfig.getConfig().getInt("threshold");
      this.banCommand = this.banConfig.getConfig().getString("ban_command");
   }

   public int aacBanVL(OfflinePlayer offlinePlayer, int i, String str) {
      OfflinePlayer offlinePlayer2 = offlinePlayer;
      ConfigurationSection section = this.getOrCreatePlayerSection(offlinePlayer2);
      Object obj = null;
      ConfigurationSection section2 = section;
      this.cleanupSection(section2);
      String str2 = str;
      int value = i;
      ConfigurationSection section3 = section;
      this.appendViolationEntry(section3, value, str2);
      ConfigurationSection section4 = section;
      int value2 = this.sumActiveViolations(section4);
      if (value2 >= this.banThreshold) {
         String str3 = this.banCommand.replace("{player}", Objects.requireNonNull(offlinePlayer.getName()));
         obj = Bukkit.getServer();
         ConsoleCommandSender consoleCommandSender = Bukkit.getConsoleSender();
         String str4 = str3;
         ConsoleCommandSender consoleCommandSender2 = consoleCommandSender;
         Server server = (Server)obj;
         server.dispatchCommand(consoleCommandSender2, str4);
         this.ioExecutor.execute(() -> {
            this.saveDetailFile(section, str3);
            this.clearPlayerHistory(offlinePlayer);
         });
      }

      return value2;
   }

   public int getBanThreshold() {
      return this.banThreshold;
   }

   private long getExpirationCutoffMillis() {
      return System.currentTimeMillis() - this.expirationMillis;
   }

   private int sumActiveViolations(ConfigurationSection section) {
      long ticks = this.getExpirationCutoffMillis();
      int value = 0;

      for (String str : section.getKeys(false)) {
         if (!str.equals("name") && Long.parseLong(str) >= ticks) {
            value += section.getConfigurationSection(str).getInt("vl");
         }
      }

      return value;
   }

   private void clearPlayerHistory(OfflinePlayer offlinePlayer) {
      ConfigurationSection section = Objects.requireNonNull(
         this.banConfig.getConfig().getConfigurationSection("players"), "Missing players section in bans.yml"
      );
      section.set(offlinePlayer.getUniqueId().toString(), null);
   }

   private ConfigurationSection getOrCreatePlayerSection(OfflinePlayer offlinePlayer) {
      ConfigurationSection section = Objects.requireNonNull(
         this.banConfig.getConfig().getConfigurationSection("players"), "Missing players section in bans.yml"
      );
      if (!section.contains(offlinePlayer.getUniqueId().toString())) {
         section.createSection(offlinePlayer.getUniqueId().toString());
      }

      ConfigurationSection section2 = Objects.requireNonNull(
         section.getConfigurationSection(offlinePlayer.getUniqueId().toString()), "configuration section missing for " + offlinePlayer.getUniqueId().toString()
      );
      section2.set("name", offlinePlayer.getName());
      return section2;
   }

   private void appendViolationEntry(ConfigurationSection section, int i, String str) {
      long now = System.currentTimeMillis();
      ConfigurationSection section2 = section.createSection(Long.toString(now));
      section2.set("vl", i);
      section2.set("info", str);
   }

   private void cleanupSection(ConfigurationSection section) {
      long ticks = this.getExpirationCutoffMillis();
      ArrayList<String> list = new ArrayList<>();

      for (String str : section.getKeys(false)) {
         if (!str.equals("name") && Long.parseLong(str) < ticks) {
            list.add(str);
         }
      }

      list.forEach(s -> section.set(s, null));
   }

   private void saveDetailFile(ConfigurationSection section, String str) {
      File file = new File(this.plugin.getDataFolder(), "bans");
      File file2 = file;
      if (!file2.isDirectory()) {
         File file3 = file;
         if (!file3.mkdir()) {
            this.plugin.getLogger().warning("Failed to create bans directory");
            return;
         }
      }

      List<Long> list = section.getKeys(false)
         .stream()
         .filter(key -> !key.equals("name"))
         .mapToLong(Long::parseLong)
         .sorted()
         .boxed()
         .collect(Collectors.toList());
      File file4 = new File(file, section.get("name") + "-" + section.getName() + ".log");

      try {
         PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file4, true)));
         Throwable throwable = null;
         boolean flag = false;

         try {
            flag = true;
            Date date = new Date();
            String str2 = "at: " + BAN_LOG_DATE_FORMAT.format(date);
            PrintWriter printWriter2 = printWriter;
            printWriter2.println(str2);
            String str3 = "    executed: " + str;
            PrintWriter printWriter3 = printWriter;
            printWriter3.println(str3);
            String str4 = "    cause: ";
            PrintWriter printWriter4 = printWriter;
            printWriter4.println(str4);

            for (Long longVal : list) {
               ConfigurationSection section2 = Objects.requireNonNull(section.getConfigurationSection(longVal.toString()));
               String str5 = "        " + BAN_LOG_DATE_FORMAT.format(new Date(longVal)) + ": (" + section2.getInt("vl") + "VL) " + section2.getString("info");
               PrintWriter printWriter5 = printWriter;
               printWriter5.println(str5);
            }

            flag = false;
         } catch (Throwable ex) {
            throwable = ex;
            throw ex;
         } finally {
            if (flag) {
               if (throwable != null) {
                  try {
                     PrintWriter printWriter6 = printWriter;
                     printWriter6.close();
                  } catch (Throwable ex2) {
                     throwable.addSuppressed(ex2);
                  }
               } else {
                  PrintWriter printWriter7 = printWriter;
                  printWriter7.close();
               }
            }
         }

         PrintWriter printWriter8 = printWriter;
         printWriter8.close();
      } catch (IOException ex3) {
         this.plugin.getLogger().warning("Failed to save ban detail file.");
         ex3.printStackTrace();
      }
   }

   @Override
   public void close() {
      ConfigurationSection section = Objects.requireNonNull(
         this.banConfig.getConfig().getConfigurationSection("players"), "Missing players section in bans.yml"
      );
      ArrayList<String> list = new ArrayList<>();

      for (String str : section.getKeys(false)) {
         ConfigurationSection section2 = Objects.requireNonNull(section.getConfigurationSection(str));
         ConfigurationSection section3 = section2;
         this.cleanupSection(section3);
         if (section2.getKeys(false).size() <= 1) {
            list.add(str);
         }
      }

      for (String str2 : list) {
         section.set(str2, null);
      }

      this.banConfig.saveConfig();
   }
}
