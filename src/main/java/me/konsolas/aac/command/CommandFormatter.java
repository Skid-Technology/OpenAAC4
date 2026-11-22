package me.konsolas.aac.command;


import java.text.SimpleDateFormat;
import java.util.Date;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandFormatter {
   private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("dd-MM-yy HH:mm:ss z");

   public static void sendHeader(CommandSender sender, String str) {
      str = str.replaceAll("\\{", ChatColor.GRAY.toString());
      str = str.replaceAll("}", ChatColor.GREEN.toString());
      sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + " *** " + ChatColor.GREEN + "-- " + str + " --");
   }

   public static void sendInfo(CommandSender sender, String str) {
      str = str.replaceAll("\\{", ChatColor.GRAY.toString());
      str = str.replaceAll("}", ChatColor.WHITE.toString());
      sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + " AAC " + ChatColor.RESET + ChatColor.WHITE + str);
   }

   public static void sendError(CommandSender sender, String str) {
      str = str.replaceAll("\\{", ChatColor.GRAY.toString());
      str = str.replaceAll("}", ChatColor.YELLOW.toString());
      sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + " AAC " + ChatColor.RESET + ChatColor.YELLOW + str);
   }

   public static String formatTimestamp(long ticks) {
      return TIMESTAMP_FORMAT.format(new Date(ticks));
   }
}
