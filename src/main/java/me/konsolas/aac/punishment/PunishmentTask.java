package me.konsolas.aac.punishment;

import java.util.List;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.CheckPlayerData;
import me.konsolas.aac.checks.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PunishmentTask implements Runnable {
   public final PunishmentManager punishmentManager;

   public PunishmentTask(PunishmentManager punishmentManager) {
      this.punishmentManager = punishmentManager;
   }

   @Override
   public void run() {
      for (Player player : Bukkit.getServer().getOnlinePlayers()) {
         for (HackType type : HackType.values()) {
            CheckManager checkManager = PunishmentManager.getPlugin(this.punishmentManager).getCheckManager();
            if (checkManager.isCheckEnabled(type)
               && PunishmentManager.getCommandsByCheck(this.punishmentManager).get(type) != null) {
               checkManager.getPlayerData(player).ifPresent(data -> this.checkThresholds(type, player, data));
            }
         }
      }
   }

   private void checkThresholds(HackType type, Player player, PlayerData playerData) {
      CheckPlayerData checkData = playerData.getCheckData(type);
      if (checkData != null) {
         int vl = checkData.violationLevel;
         List<ThresholdCommand> commands = PunishmentManager.getCommandsByCheck(this.punishmentManager).get(type);
         for (ThresholdCommand command : commands) {
            if (command.shouldTrigger(player, vl)) {
               Bukkit.getServer()
                  .getScheduler()
                  .scheduleSyncDelayedTask(
                     PunishmentManager.getPlugin(this.punishmentManager),
                     new ConsoleCommandTask(this, command, player, type)
                  );
            }
         }
      }
   }
}
