package me.konsolas.aac.gui;


import me.konsolas.aac.AAC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class ContextClickMenuItem extends MenuItem {
   private MenuCallback<MenuClick> clickHandler;

   public ContextClickMenuItem(MenuCallback<MenuClick> menuCallback) {
      super();
      this.clickHandler = new EmptyMenuCallback(this);
      if (menuCallback != null) {
         this.clickHandler = menuCallback;
      }
   }

   @Override
   public void onClick(Player player) {
      ContextClickMenuItem contextClickMenuItem = this;
      BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
      AAC plugin = AAC.getInstance();
      DeferredContextClickTask deferredContextClickTask = new DeferredContextClickTask(this, player, contextClickMenuItem);
      AAC plugin2 = plugin;
      BukkitScheduler bukkitScheduler2 = bukkitScheduler;
      bukkitScheduler2.runTask(plugin2, deferredContextClickTask);
   }

   public static MenuCallback<MenuClick> getClickHandler(ContextClickMenuItem contextClickMenuItem) {
      return contextClickMenuItem.clickHandler;
   }
}
