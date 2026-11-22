package me.konsolas.aac.punishment;


import me.konsolas.aac.AAC;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class ViolationLogger {
   private final AAC plugin;
   private final boolean debug;
   private final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
   private final SimpleDateFormat messageDateFormat;
   private PrintWriter writer;
   private Thread writerThread;
   private final ArrayBlockingQueue<String> pendingLines;
   private final Map<Player, Set<String>> subscribers;

   public ViolationLogger(AAC plugin, boolean flag) {
      this.messageDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
      this.pendingLines = new ArrayBlockingQueue<>(512);
      this.subscribers = new WeakHashMap<>();
      this.plugin = plugin;
      this.debug = flag;
   }

   public void logSystem(String str) {
      ConsoleCommandSender consoleCommandSender = Bukkit.getConsoleSender();
      String str2 = str;
      ConsoleCommandSender consoleCommandSender2 = consoleCommandSender;
      this.logViolation(consoleCommandSender2, str2);
   }

   public void logViolation(CommandSender sender, String str) {
      if (this.writer != null) {
         Date date = new Date();
         this.pendingLines.add("[" + this.messageDateFormat.format(date) + "] [" + sender.getName() + "] " + str);

         for (Entry<Player, Set<String>> entry : this.subscribers.entrySet()) {
            if (entry.getValue().isEmpty() || entry.getValue().contains(sender.getName())) {
               entry.getKey().sendMessage(" * §4[" + sender.getName() + "] §7" + str);
            }
         }

         if (this.debug) {
            Bukkit.getConsoleSender().sendMessage("[AAC] [" + sender.getName() + "] " + str);
         }
      }
   }

   public String toggleSubscription(Player player, Set<String> set) {
      HashSet<String> set2 = new HashSet<>(set);
      if (!this.subscribers.containsKey(player)) {
         this.subscribers.put(player, set2);
      } else {
         this.subscribers.get(player).removeIf(set2::remove);
         this.subscribers.get(player).addAll(set2);
         if (this.subscribers.get(player).isEmpty() || set.isEmpty()) {
            this.subscribers.remove(player);
         }
      }

      return this.subscribers.containsKey(player) ? (this.subscribers.get(player).isEmpty() ? "all" : this.subscribers.get(player).toString()) : "none";
   }

   public void compressOldLogs() {
      File file2 = new File(this.plugin.getDataFolder(), "logs");
      if (file2.exists()) {
         FileFilter fileFilter = file -> file.isFile() && file.getName().endsWith(".log");
         File file3 = file2;
         File[] fileArray = file3.listFiles(fileFilter);
         if (fileArray != null && fileArray.length > 0) {
            this.plugin.getLogger().info("Compressing " + fileArray.length + " log file" + (fileArray.length > 1 ? "s" : ""));

            for (File file4 : fileArray) {
               try {
                  StringBuilder sb = new StringBuilder();
                  File file5 = file4;
                  GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(new File(file2, sb.append(file5.getName()).append(".gz").toString())));
                  Throwable throwable = null;
                  boolean flag = false;

                  try {
                     flag = true;
                     File file6 = file4;
                     Files.copy(file6.toPath(), gzipOutputStream);
                     File file7 = file4;
                     if (!file7.delete()) {
                        throw new IOException("could not delete original log file");
                     }

                     flag = false;
                  } catch (Throwable ex) {
                     throwable = ex;
                     throw ex;
                  } finally {
                     if (flag) {
                        if (throwable != null) {
                           try {
                              GZIPOutputStream gzipOutputStream2 = gzipOutputStream;
                              gzipOutputStream2.close();
                           } catch (Throwable ex2) {
                              throwable.addSuppressed(ex2);
                           }
                        } else {
                           GZIPOutputStream gzipOutputStream3 = gzipOutputStream;
                           gzipOutputStream3.close();
                        }
                     }
                  }

                  GZIPOutputStream gzipOutputStream4 = gzipOutputStream;
                  gzipOutputStream4.close();
               } catch (IOException ex3) {
                  Logger logger = this.plugin.getLogger();
                  StringBuilder sb2 = new StringBuilder().append("Failed to compress ");
                  File file8 = file4;
                  logger.warning(sb2.append(file8.getName()).append(": ").append(ex3.getMessage()).toString());
               }
            }
         }
      }
   }

   public void openLog() {
      try {
         if (!this.plugin.getConfig().getBoolean("log_file", true)) {
            this.plugin.getLogger().warning("Logger functionality has been disabled in the configuration file.");
            return;
         }

         File file2 = new File(this.plugin.getDataFolder(), "logs");
         File file3 = file2;
         if (!file3.exists()) {
            File file4 = file2;
            if (!file4.mkdir()) {
               throw new IOException("Failed to create data dir");
            }
         }

         Date date = new Date();
         File file5 = new File(this.plugin.getDataFolder(), "logs/aac_" + this.fileDateFormat.format(date) + ".log");
         File file6 = file5;
         if (!file6.exists()) {
            File file7 = file5;
            if (!file7.createNewFile()) {
               throw new IOException("Failed to create new file");
            }
         }

         this.writer = new PrintWriter(new FileWriter(file5, true), true);
      } catch (IOException ex) {
         this.plugin.getLogger().severe(" Error: Could not setup log file for writing");
         this.plugin.getLogger().severe(ex.getMessage());
         this.plugin.getLogger().severe("Logger functionality has been disabled.");
      }

      this.writerThread = new LogWriterThread(this, "AAC Logging I/O Thread");
      this.writerThread.start();
   }

   public void closeLog() {
      try {
         this.plugin.getLogger().info("Saving log file...");
         this.writerThread.interrupt();
         PrintWriter printWriter = this.writer;
         printWriter.close();
      } catch (NullPointerException ex) {
         this.plugin.getLogger().warning("Could not save log file as none was created.");
      }
   }

   public static ArrayBlockingQueue<String> getPendingLines(ViolationLogger violationLogger) {
      return violationLogger.pendingLines;
   }

   public static PrintWriter getWriter(ViolationLogger violationLogger) {
      return violationLogger.writer;
   }

   public static AAC getPlugin(ViolationLogger violationLogger) {
      return violationLogger.plugin;
   }
}
