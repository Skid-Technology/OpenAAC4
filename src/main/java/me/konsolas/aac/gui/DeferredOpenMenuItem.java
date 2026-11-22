package me.konsolas.aac.gui;


import me.konsolas.aac.AAC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class DeferredOpenMenuItem extends CallbackMenuItem {
   private InventoryMenu targetMenu;

   public DeferredOpenMenuItem(InventoryMenu inventoryMenu) {
      super(null);
      this.targetMenu = inventoryMenu;
      this.targetMenu.setEnabled(false);
   }

   @Override
   public void onClick(Player player) {
      Player player2 = player;
      super.onClick(player2);
      BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
      AAC plugin = AAC.getInstance();
      MenuOpenTask menuOpenTask = new MenuOpenTask(this, player);
      AAC plugin2 = plugin;
      BukkitScheduler bukkitScheduler2 = bukkitScheduler;
      bukkitScheduler2.runTask(plugin2, menuOpenTask);
   }

   public static InventoryMenu getTargetMenu(DeferredOpenMenuItem deferredOpenMenuItem) {
      return deferredOpenMenuItem.targetMenu;
   }
}
