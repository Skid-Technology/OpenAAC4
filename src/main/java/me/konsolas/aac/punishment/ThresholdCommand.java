package me.konsolas.aac.punishment;

import java.util.HashMap;
import java.util.Map;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.PlayerViolationCommandEvent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

public class ThresholdCommand {
   private final int threshold;
   private final String commandTemplate;
   private final Map armedPlayers;
   public final PunishmentManager punishmentManager;

   public ThresholdCommand(PunishmentManager punishmentManager, int i, String str) {
      this.punishmentManager = punishmentManager;
      this.armedPlayers = new HashMap();
      this.threshold = i;
      this.commandTemplate = str;
   }

   public void execute(Player player, HackType hackType) {
      PlayerViolationCommandEvent event = new PlayerViolationCommandEvent(player, hackType, this.commandTemplate);
      PluginManager pluginManager = Bukkit.getServer().getPluginManager();
      PlayerViolationCommandEvent event2 = event;
      PluginManager pluginManager2 = pluginManager;
      pluginManager2.callEvent(event2);
      if (!event.isCancelled()) {
         Server server = Bukkit.getServer();
         ConsoleCommandSender consoleCommandSender = Bukkit.getConsoleSender();
         PunishmentManager punishmentManager = this.punishmentManager;
         String str = event.getCommand();
         Player player2 = player;
         String str2 = str;
         PunishmentManager punishmentManager2 = punishmentManager;
         String str3 = PunishmentManager.formatPlaceholders(punishmentManager2, str2, player2);
         ConsoleCommandSender consoleCommandSender2 = consoleCommandSender;
         Server server2 = server;
         server2.dispatchCommand(consoleCommandSender2, str3);
      }
   }

   public boolean shouldTrigger(Player player, int i) {
      if (!this.armedPlayers.containsKey(player.getUniqueId())) {
         this.armedPlayers.put(player.getUniqueId(), true);
      }

      if ((Boolean)this.armedPlayers.get(player.getUniqueId())) {
         if (i > this.threshold) {
            this.armedPlayers.put(player.getUniqueId(), false);
            return true;
         }
      } else if (i < this.threshold) {
         this.armedPlayers.put(player.getUniqueId(), true);
      }

      return false;
   }

   public String getCommand() {
      return this.commandTemplate;
   }
}
