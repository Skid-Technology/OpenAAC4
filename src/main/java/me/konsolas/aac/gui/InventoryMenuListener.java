package me.konsolas.aac.gui;

import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryMenuListener implements Listener {
   public final InventoryMenu inventoryMenuValue;

   private InventoryMenuListener(InventoryMenu inventoryMenu) {
      this.inventoryMenuValue = inventoryMenu;
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         if (InventoryMenu.getOpenMenus().containsKey(player.getUniqueId())) {
            InventoryMenu inventoryMenu = (InventoryMenu)InventoryMenu.getOpenMenus().get(player.getUniqueId());
            if (event.getInventory().equals(inventoryMenu.getInventory())) {
               boolean flag = false;
               if (event.getClick() == ClickType.NUMBER_KEY) {
                  MenuItem menuItem = InventoryMenu.findHotkeyItem(inventoryMenu, event.getHotbarButton());
                  if (menuItem != null) {
                     flag = true;
                     Player player2 = player;
                     menuItem.onClick(player2);
                     if (menuItem.shouldCancelClick()) {
                        event.setCancelled(true);
                     }
                  }

                  if (!flag && inventoryMenu.isCancelUnhandledClicks()) {
                     event.setCancelled(true);
                  }

                  return;
               }

               ItemStack item = event.getCurrentItem();
               if (item == null) {
                  return;
               }

               for (MenuItem menuItem2 : new ArrayList<>(inventoryMenu.getItems())) {
                  if (menuItem2.getSlot() == event.getSlot()) {
                     flag = true;
                     Player player3 = player;
                     menuItem2.onClick(player3);
                     if (menuItem2.shouldCancelClick()) {
                        event.setCancelled(true);
                     }
                  }
               }

               if (!flag && inventoryMenu.isCancelUnhandledClicks()) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   public InventoryMenuListener(InventoryMenu inventoryMenu, InventoryCloseTask inventoryCloseTask) {
      this(inventoryMenu);
   }
}
