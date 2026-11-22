package me.konsolas.aac.api.internal;


import me.konsolas.aac.AAC;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.compat.PlayerPingAccess;
import me.konsolas.aac.punishment.ViolationLogger;
import me.konsolas.aac.util.TickRateMonitor;
import me.konsolas.aac.api.AACAPI;
import me.konsolas.aac.api.HackType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AacApiImplementation implements AACAPI {
   public final AAC enabled;

   public AacApiImplementation(AAC plugin) {
      this.enabled = plugin;
   }

   @Override
   public void reloadAAC() {
      this.enabled.reloadPlugin();
   }

   @Override
   public void enableCheck(HackType hackType) {
      CheckManager checkManager = this.enabled.getCheckManager();
      HackType hackType2 = hackType;
      checkManager.enableCheck(hackType2);
   }

   @Override
   public void disableCheck(HackType hackType) {
      this.enabled.getCheckManager().disableCheck(hackType);
   }

   @Override
   public boolean isEnabled(HackType hackType) {
      return this.enabled.getCheckManager().isCheckEnabled(hackType);
   }

   @Override
   public double getTPS() {
      return TickRateMonitor.getTicksPerSecond();
   }

   @Override
   public int getPing(Player player) {
      Player player2 = player;
      return PlayerPingAccess.getPing(player2);
   }

   @Override
   public int getViolationLevel(Player player, HackType hackType) {
      CheckManager checkManager = this.enabled.getCheckManager();
      Player player2 = player;
      return ((PlayerData)checkManager.getPlayerData(player2).orElseThrow(IllegalArgumentException::new)).getCheckData(hackType).violationLevel;
   }

   @Override
   public void setViolationLevel(Player player, HackType hackType, int i) {
      CheckManager checkManager = this.enabled.getCheckManager();
      Player player2 = player;
      ((PlayerData)checkManager.getPlayerData(player2).orElseThrow(IllegalAccessError::new)).getCheckData(hackType).violationLevel = i;
   }

   @Override
   public void log(CommandSender sender, String str) {
      ViolationLogger violationLogger = this.enabled.getViolationLogger();
      String str2 = "API: " + str;
      CommandSender sender2 = sender;
      violationLogger.logViolation(sender2, str2);
   }

}
