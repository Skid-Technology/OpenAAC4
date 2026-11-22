package me.konsolas.aac.gui;


import me.konsolas.aac.util.ItemBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PaginatedInventoryMenu extends InventoryMenu {
   private final int itemsPerPage;
   private final int pageCount;
   private final List items;
   private final MenuItem nextButton;
   private final MenuItem backButton;
   private List pages;
   private final InventoryMenu parentMenu;

   public PaginatedInventoryMenu(String str, int i, int i2, List list, MenuItem menuItem, MenuItem menuItem2, InventoryMenu inventoryMenu) {
      super(str, i);
      this.itemsPerPage = i2;
      this.items = list;
      this.nextButton = menuItem;
      this.backButton = menuItem2;
      this.pageCount = list.size() / i2 + 1;
      this.parentMenu = inventoryMenu;
      this.buildPages();
   }

   private void buildPages() {
      Iterator iterator = this.items.iterator();
      this.pages = new ArrayList();
      int value = 0;

      for (int value2 = 0; value2 < this.pageCount; value2++) {
         int value3 = this.getSize();
         InventoryMenu inventoryMenu = new InventoryMenu(value3);
         inventoryMenu.setTitle(this.getTitle());

         while (iterator.hasNext() && value < this.itemsPerPage) {
            MenuItem menuItem = (MenuItem)iterator.next();
            menuItem.setSlot(value);
            MenuItem menuItem2 = menuItem;
            inventoryMenu.addItem(menuItem2);
            value++;
         }

         this.pages.add(inventoryMenu);
         value = 0;
      }

      for (int value4 = 0; value4 < this.pages.size(); value4++) {
         InventoryMenu inventoryMenu2 = (InventoryMenu)this.pages.get(value4);
         if (value4 != 0) {
            DeferredOpenMenuItem deferredOpenMenuItem = new DeferredOpenMenuItem((InventoryMenu)this.pages.get(value4 - 1));
            deferredOpenMenuItem.setDisplayName(this.backButton.getDisplayName())
               .setItemStack(this.backButton.buildItemStack())
               .setSlot(this.backButton.getSlot())
               .setHotkey(0);
            DeferredOpenMenuItem deferredOpenMenuItem2 = deferredOpenMenuItem;
            inventoryMenu2.addItem(deferredOpenMenuItem2);
         }

         if (this.parentMenu != null) {
            OpenMenuItem openMenuItem = new OpenMenuItem(this.parentMenu);
            openMenuItem.setItemStack(new ItemBuilder(Material.BARRIER).setLore(new String[]{"§8HOTKEY: 9"}))
               .setDisplayName("Back to main menu")
               .setSlot(this.itemsPerPage + 4);
            openMenuItem.setHotkey(8);
            OpenMenuItem openMenuItem2 = openMenuItem;
            inventoryMenu2.addItem(openMenuItem2);
         }

         if (value4 + 1 < this.pageCount) {
            DeferredOpenMenuItem deferredOpenMenuItem3 = new DeferredOpenMenuItem((InventoryMenu)this.pages.get(value4 + 1));
            deferredOpenMenuItem3.setDisplayName(this.nextButton.getDisplayName())
               .setItemStack(this.nextButton.buildItemStack())
               .setSlot(this.nextButton.getSlot())
               .setHotkey(1);
            DeferredOpenMenuItem deferredOpenMenuItem4 = deferredOpenMenuItem3;
            inventoryMenu2.addItem(deferredOpenMenuItem4);
         }
      }
   }

   @Override
   public InventoryMenu open(Player player) {
      super.open(player);
      return ((InventoryMenu)this.pages.get(0)).open(player);
   }
}
