package me.konsolas.aac.gui;


import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class MenuItem {
   private static final String[] EMPTY_LORE = new String[0];
   protected String displayName = "this_is_a_default_value";
   protected Object[] displayArguments;
   protected String[] lore = EMPTY_LORE;
   protected int slot = 0;
   protected ItemStack itemStack = new ItemStack(Material.STONE);
   protected boolean cancelClick = true;
   private int hotkey = -1;

   public ItemStack buildItemStack() {
      ItemMeta itemMeta = this.itemStack.getItemMeta();
      if (!this.displayName.equals("this_is_a_default_value")) {
         itemMeta.setDisplayName(this.displayName);
      }

      if (this.lore.length != EMPTY_LORE.length) {
         itemMeta.setLore(Arrays.asList(this.lore));
      }

      this.itemStack.setItemMeta(itemMeta);
      return this.itemStack;
   }

   public MenuItem setDisplayName(String str) {
      this.displayName = str;
      this.displayArguments = new Object[0];
      return this;
   }

   public abstract void onClick(Player player);

   public String getDisplayName() {
      return this.displayName;
   }

   public int getSlot() {
      return this.slot;
   }

   public MenuItem setSlot(int i) {
      this.slot = i;
      return this;
   }

   public boolean shouldCancelClick() {
      return this.cancelClick;
   }

   public int getHotkey() {
      return this.hotkey;
   }

   public MenuItem setHotkey(int i) {
      this.hotkey = i;
      return this;
   }

   public MenuItem setItemStack(ItemStack item) {
      this.itemStack = item;
      return this;
   }

}
