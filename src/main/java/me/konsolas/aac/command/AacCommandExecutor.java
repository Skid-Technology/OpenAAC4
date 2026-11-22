package me.konsolas.aac.command;


import me.konsolas.aac.AAC;
import me.konsolas.aac.gui.CheckMenuFactory;
import me.konsolas.aac.gui.ViolationMenuFactory;
import me.konsolas.aac.punishment.BanManager;
import me.konsolas.aac.punishment.ViolationLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class AacCommandExecutor implements TabExecutor {
   private final AAC plugin;

   public AacCommandExecutor(AAC plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String str, String[] strArray) {
      if (strArray.length == 0) {
         String str2 = "AAC {" + AAC.getInstance().getDescription().getVersion() + "} ~konsolas";
         CommandSender sender2 = sender;
         CommandFormatter.sendInfo(sender2, str2);
         return true;
      }

      String str3 = strArray[0].toLowerCase();
      if (!str3.equals("reload")) {
         if (!str3.equals("status")) {
            if (!str3.equals("manage")) {
               if (!str3.equals("notify")) {
                  if (!str3.equals("moderator")) {
                     if (!str3.equals("verbose")) {
                        if (!str3.equals("banvl")) {
                           return false;
                        }

                        if (!sender.hasPermission("AAC.banvl")) {
                           String str4 = "Permission denied";
                           CommandSender sender3 = sender;
                           CommandFormatter.sendError(sender3, str4);
                           return true;
                        }

                        if (strArray.length <= 2) {
                           String str5 = "Usage: {/aac banvl <vl> <player> <info...>}";
                           CommandSender sender4 = sender;
                           CommandFormatter.sendError(sender4, str5);
                           return true;
                        }

                        int i;
                        try {
                           i = Integer.parseInt(strArray[1]);
                        } catch (NumberFormatException ex) {
                           String str6 = "Failed to parse VL: {" + ex.getMessage() + "}";
                           CommandSender sender5 = sender;
                           CommandFormatter.sendError(sender5, str6);
                           return true;
                        }

                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(strArray[2]);
                        if (!offlinePlayer.hasPlayedBefore()) {
                           String str7 = "Player {" + strArray[2] + "} not found";
                           CommandSender sender6 = sender;
                           CommandFormatter.sendError(sender6, str7);
                           return true;
                        } else {
                           BanManager banManager = this.plugin.banManager;
                           String str8 = Arrays.stream(strArray).skip(3L).collect(Collectors.joining(" "));
                           int value = i;
                           OfflinePlayer offlinePlayer2 = offlinePlayer;
                           int value2 = banManager.aacBanVL(offlinePlayer2, value, str8);
                           String str9 = "Added {" + i + "VL} to {" + offlinePlayer.getName() + "}";
                           CommandSender sender7 = sender;
                           CommandFormatter.sendInfo(sender7, str9);
                           String str10 = "New VL: {" + value2 + "}/{" + this.plugin.banManager.getBanThreshold() + "}";
                           CommandSender sender8 = sender;
                           CommandFormatter.sendInfo(sender8, str10);
                           return true;
                        }
                     } else if (!sender.hasPermission("AAC.verbose")) {
                        String str11 = "Permission denied";
                        CommandSender sender9 = sender;
                        CommandFormatter.sendError(sender9, str11);
                        return true;
                     } else if (!(sender instanceof Player)) {
                        String str12 = "Cannot disable verbose output for console";
                        CommandSender sender10 = sender;
                        CommandFormatter.sendError(sender10, str12);
                        return true;
                     } else {
                        HashSet<String> set = new HashSet<>();
                        Arrays.stream(strArray).skip(1L).forEach(target -> {
                           if (target.equalsIgnoreCase("CONSOLE")) {
                              set.add("CONSOLE");
                           } else {
                              Player matchedPlayer = Bukkit.getPlayerExact(target);
                              if (matchedPlayer != null) {
                                 set.add(matchedPlayer.getName());
                              } else {
                                 CommandFormatter.sendError(sender, "Unknown stream: {" + target + "}");
                              }
                           }
                        });
                        StringBuilder sb = new StringBuilder().append("Now receiving verbose output for: {");
                        ViolationLogger violationLogger = this.plugin.getViolationLogger();
                        Player player = (Player)sender;
                        HashSet set2 = set;
                        Player player2 = player;
                        String str13 = sb.append(violationLogger.toggleSubscription(player2, set2)).append("}").toString();
                        CommandSender sender11 = sender;
                        CommandFormatter.sendInfo(sender11, str13);
                        return true;
                     }
                  } else if (!sender.hasPermission("AAC.moderator")) {
                     String str14 = "Permission denied";
                     CommandSender sender12 = sender;
                     CommandFormatter.sendError(sender12, str14);
                     return true;
                  } else if (!(sender instanceof Player)) {
                     String str15 = "Cannot show GUI to console";
                     CommandSender sender13 = sender;
                     CommandFormatter.sendError(sender13, str15);
                     return true;
                  } else {
                     Player player3 = (Player)sender;
                     ViolationMenuFactory.open(player3);
                     return true;
                  }
               } else if (!sender.hasPermission("AAC.notify")) {
                  String str16 = "Permission denied";
                  CommandSender sender14 = sender;
                  CommandFormatter.sendError(sender14, str16);
                  return true;
               } else {
                  Bukkit.broadcast(
                     ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(this.plugin.getConfig().getString("staff_chat_format")))
                           .replace("{sender}", sender.getName())
                        + Arrays.stream(strArray).skip(1L).collect(Collectors.joining(" ")),
                     "AAC.notify"
                  );
                  return true;
               }
            } else if (!sender.hasPermission("AAC.manage")) {
               String str17 = "Permission denied";
               CommandSender sender15 = sender;
               CommandFormatter.sendError(sender15, str17);
               return true;
            } else if (!(sender instanceof Player)) {
               String str18 = "Cannot show GUI to console";
               CommandSender sender16 = sender;
               CommandFormatter.sendError(sender16, str18);
               return true;
            } else {
               Player player4 = (Player)sender;
               CheckMenuFactory.open(player4);
               return true;
            }
         } else if (!sender.hasPermission("AAC.status")) {
            String str19 = "Permission denied";
            CommandSender sender17 = sender;
            CommandFormatter.sendError(sender17, str19);
            return true;
         } else {
            String now = "AAC Status: {" + CommandFormatter.formatTimestamp(System.currentTimeMillis()) + "}";
            CommandSender sender18 = sender;
            CommandFormatter.sendHeader(sender18, now);
            String str20 = "Players Online: " + Bukkit.getOnlinePlayers().size();
            CommandSender sender19 = sender;
            CommandFormatter.sendInfo(sender19, str20);
            String str21 = "Checks Enabled: " + this.plugin.getCheckManager().getEnabledCheckTypes();
            CommandSender sender20 = sender;
            CommandFormatter.sendInfo(sender20, str21);
            return true;
         }
      } else if (!sender.hasPermission("AAC.reload")) {
         String str22 = "Permission denied";
         CommandSender sender21 = sender;
         CommandFormatter.sendError(sender21, str22);
         return true;
      } else {
         String str23 = "Reload request registered.";
         CommandSender sender22 = sender;
         CommandFormatter.sendInfo(sender22, str23);
         this.plugin.reloadPlugin();
         return true;
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String str, String[] strArray) {
      List<String> list = Stream.of("reload", "status", "manage", "notify", "moderator", "verbose")
         .filter(subcommand -> sender.hasPermission("AAC." + subcommand))
         .collect(Collectors.toList());
      if (strArray.length <= 0) {
         return list;
      }

      if (strArray.length == 1) {
         ArrayList<String> list2 = new ArrayList<>();
         StringUtil.copyPartialMatches(strArray[0], list, list2);
         return list2;
      }

      if ((!strArray[0].equals("notify") || !list.contains("notify")) && (!strArray[0].equals("verbose") || !list.contains("verbose"))) {
         return Collections.emptyList();
      }

      ArrayList<String> list3 = new ArrayList<>();
      StringUtil.copyPartialMatches(
         strArray[strArray.length - 1],
         Stream.concat(Stream.of(Bukkit.getConsoleSender().getName()), Bukkit.getOnlinePlayers().stream().map(OfflinePlayer::getName))
            .collect(Collectors.toList()),
         list3
      );
      return list3;
   }
}
