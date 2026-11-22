package me.konsolas.aac.gui;

import org.bukkit.entity.Player;

public class OpenMenuItem extends CallbackMenuItem {
   private InventoryMenu targetMenu;

   public OpenMenuItem() {
      super(null);
   }

   public OpenMenuItem(InventoryMenu inventoryMenu) {
      super(null);
      this.targetMenu = inventoryMenu;
   }

   @Override
   public void onClick(Player player) {
      Player player2 = player;
      super.onClick(player2);
      if (this.targetMenu != null) {
         this.targetMenu.open(player);
      }
   }
}
