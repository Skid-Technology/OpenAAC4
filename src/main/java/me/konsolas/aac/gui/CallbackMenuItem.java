package me.konsolas.aac.gui;


import me.konsolas.aac.AAC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class CallbackMenuItem extends MenuItem {
   private final Plugin plugin;
   private MenuCallback<Player> clickHandler;

   public CallbackMenuItem(MenuCallback<Player> menuCallback) {
      this(menuCallback, AAC.getInstance());
   }

   public CallbackMenuItem(MenuCallback<Player> menuCallback, Plugin plugin) {
      this.clickHandler = player -> {};
      this.plugin = plugin;
      if (menuCallback != null) {
         this.clickHandler = menuCallback;
      }
   }

   @Override
   public void onClick(Player player) {
      BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
      Plugin plugin = this.plugin;
      Runnable runnable = () -> {
         if (player.isOnline()) {
            this.clickHandler.accept(player);
         }
      };
      Plugin plugin2 = plugin;
      BukkitScheduler bukkitScheduler2 = bukkitScheduler;
      bukkitScheduler2.scheduleSyncDelayedTask(plugin2, runnable);
   }
}
