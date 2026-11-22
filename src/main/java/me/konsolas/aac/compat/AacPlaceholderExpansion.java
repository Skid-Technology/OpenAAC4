package me.konsolas.aac.compat;


import me.konsolas.aac.AAC;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.CheckPlayerData;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.util.TickRateMonitor;
import java.util.Optional;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.konsolas.aac.api.HackType;
import org.bukkit.entity.Player;

public class AacPlaceholderExpansion extends PlaceholderExpansion {
   public final AAC version;

   public AacPlaceholderExpansion(AAC plugin) {
      this.version = plugin;
   }

   public boolean persist() {
      return true;
   }

   public boolean canRegister() {
      return true;
   }

   public String getIdentifier() {
      return "aac";
   }

   public String getAuthor() {
      return "konsolas";
   }

   public String getVersion() {
      return this.version.getDescription().getVersion();
   }

   public String onPlaceholderRequest(Player player, String str) {
      if (str.equals("tps")) {
         return String.format("%.1f", TickRateMonitor.getTicksPerSecond());
      }

      if (player == null) {
         return "";
      }

      if (str.startsWith("vl_")) {
         CheckManager checkManager = this.version.getCheckManager();
         Player player2 = player;
         Optional optional = checkManager.getPlayerData(player2);
         if (optional.isPresent()) {
            try {
               CheckPlayerData checkData = ((PlayerData)optional.get()).getCheckData(HackType.valueOf(str.substring(3).toUpperCase()));
               if (checkData != null) {
                  return Integer.toString(checkData.violationLevel);
               }
            } catch (IllegalArgumentException ex) {
            }
         }

         return "0";
      } else if (str.equals("player")) {
         return player.getName();
      } else if (str.equals("ping")) {
         Player player3 = player;
         return Integer.toString(PlayerPingAccess.getPing(player3));
      } else {
         return str.equals("world") ? player.getWorld().getName() : null;
      }
   }

}
