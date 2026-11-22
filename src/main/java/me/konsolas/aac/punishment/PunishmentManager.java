package me.konsolas.aac.punishment;


import me.konsolas.aac.AAC;
import me.konsolas.aac.compat.PlayerPingAccess;
import me.konsolas.aac.util.TickRateMonitor;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import me.clip.placeholderapi.PlaceholderAPI;
import me.konsolas.aac.api.HackType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public class PunishmentManager implements Listener {
   private final AAC plugin;
   private BukkitTask task;
   private final EnumMap<HackType, List<ThresholdCommand>> commandsByCheck = new EnumMap<>(HackType.class);

   public PunishmentManager(AAC plugin) {
      this.plugin = plugin;
   }

   public void loadThresholds() {
      HackType[] hackTypeArray = HackType.values();

      for (HackType hackType : hackTypeArray) {
         ArrayList<ThresholdCommand> list = new ArrayList<>();
         ConfigurationSection section = this.plugin.getConfig().getConfigurationSection(hackType.getName() + ".thresholds");
         if (section == null) {
            this.plugin.getLogger().warning("(Punisher) WARN: Failed to get thresholds for check " + hackType);
         } else {
            for (String str : section.getKeys(false)) {
               Object obj = section.get(str);

               int i;
               try {
                  i = Integer.parseInt(str);
               } catch (NumberFormatException ex) {
                  this.plugin.getLogger().warning("(Punisher) WARN: Failed to parse integer threshold: " + ex.getMessage());
                  continue;
               }

               if (obj instanceof String) {
                  ThresholdCommand thresholdCommand = new ThresholdCommand(this, i, section.getString(str));
                  list.add(thresholdCommand);
               } else {
                  List<String> list2 = section.getStringList(str);
                  if (!list2.isEmpty()) {
                     for (String str2 : list2) {
                        ThresholdCommand thresholdCommand2 = new ThresholdCommand(this, i, str2);
                        list.add(thresholdCommand2);
                     }
                  } else {
                     this.plugin.getLogger().warning("(Punisher) WARN: Failed to get multiple commands for check " + hackType + ", threshold " + i);
                  }
               }
            }

            this.commandsByCheck.put(hackType, list);
         }
      }

      this.task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, new PunishmentTask(this), 20L, 17L);
   }

   private String formatPlaceholders(String str, Player player) {
      if (this.plugin.isPlaceholderApiEnabled()) {
         return PlaceholderAPI.setPlaceholders(player, str);
      }

      str = str.replace("%aac_player%", player.getName());
      StringBuilder sb = new StringBuilder();
      Player player2 = player;
      str = str.replace("%aac_ping%", sb.append(PlayerPingAccess.getPing(player2)).append("").toString());
      str = str.replace("%aac_tps%", Math.round(TickRateMonitor.getTicksPerSecond() * 100.0) / 100.0 + "");
      return str.replace("%aac_world%", player.getWorld().getName());
   }

   public void shutdown() {
      this.task.cancel();
   }

   public static String formatPlaceholders(PunishmentManager punishmentManager, String str, Player player) {
      Player player2 = player;
      String str2 = str;
      return punishmentManager.formatPlaceholders(str2, player2);
   }

   public static AAC getPlugin(PunishmentManager punishmentManager) {
      return punishmentManager.plugin;
   }

   public static EnumMap<HackType, List<ThresholdCommand>> getCommandsByCheck(PunishmentManager punishmentManager) {
      return punishmentManager.commandsByCheck;
   }
}
