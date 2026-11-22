package me.konsolas.aac.gui;

import org.bukkit.entity.Player;

public class MenuOpenTask implements Runnable {
   public final Player player;
   public final DeferredOpenMenuItem menuItem;

   public MenuOpenTask(DeferredOpenMenuItem deferredOpenMenuItem, Player player) {
      this.menuItem = deferredOpenMenuItem;
      this.player = player;
   }

   @Override
   public void run() {
      if (this.player.isOnline()) {
         InventoryMenu inventoryMenu = DeferredOpenMenuItem.getTargetMenu(this.menuItem);
         if (inventoryMenu != null) {
            DeferredOpenMenuItem.getTargetMenu(this.menuItem).open(this.player);
         }
      }
   }
}
