package me.konsolas.aac.gui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryCloseTask implements Runnable {
   public final InventoryCloseEvent inventoryCloseEventValue;
   public final InventoryMenu inventoryMenuValue;

   public InventoryCloseTask(InventoryMenu inventoryMenu, InventoryCloseEvent event) {
      this.inventoryMenuValue = inventoryMenu;
      this.inventoryCloseEventValue = event;
   }

   @Override
   public void run() {
      HumanEntity humanEntity = this.inventoryCloseEventValue.getPlayer();
      if (humanEntity instanceof Player) {
         humanEntity = this.inventoryCloseEventValue.getPlayer();
         Player player = (Player)humanEntity;
         InventoryMenu inventoryMenu = InventoryMenu.getOpenMenus().get(player.getUniqueId());
         if (player.getOpenInventory().getType() == InventoryType.CRAFTING && inventoryMenu != null && !inventoryMenu.isRetainOnClose()) {
            MenuCallback<Object> menuCallback = inventoryMenu.getCloseHandler();
            if (menuCallback != null) {
               menuCallback = inventoryMenu.getCloseHandler();
               menuCallback.accept(null);
            }

            InventoryMenu.getOpenMenus().remove(player.getUniqueId());
         }
      }
   }
}
