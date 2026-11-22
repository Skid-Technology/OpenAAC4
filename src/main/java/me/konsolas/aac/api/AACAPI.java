package me.konsolas.aac.api;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface AACAPI {
   void reloadAAC();

   void enableCheck(HackType hackType);

   void disableCheck(HackType hackType);

   boolean isEnabled(HackType hackType);

   double getTPS();

   int getPing(Player player);

   int getViolationLevel(Player player, HackType hackType);

   void setViolationLevel(Player player, HackType hackType, int i);

   void log(CommandSender sender, String str);
}
