package me.konsolas.aac.punishment;

import me.konsolas.aac.api.HackType;
import org.bukkit.entity.Player;

public class ConsoleCommandTask implements Runnable {
   private final ThresholdCommand thresholdCommand;
   private final Player player;
   private final HackType hackType;
   public final PunishmentTask punishmentTask;

   public ConsoleCommandTask(PunishmentTask punishmentTask, ThresholdCommand thresholdCommand, Player player, HackType hackType) {
      this.punishmentTask = punishmentTask;
      this.thresholdCommand = thresholdCommand;
      this.player = player;
      this.hackType = hackType;
   }

   @Override
   public void run() {
      ViolationLogger violationLogger = PunishmentManager.getPlugin(this.punishmentTask.punishmentManager).getViolationLogger();
      String commandLog = this.hackType + ": executing command \"" + PunishmentManager.formatPlaceholders(this.punishmentTask.punishmentManager, this.thresholdCommand.getCommand(), this.player) + "\"";
      violationLogger.logViolation(this.player, commandLog);
      this.thresholdCommand.execute(this.player, this.hackType);
   }
}
