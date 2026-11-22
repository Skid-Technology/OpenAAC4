package me.konsolas.aac.util;


import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemBuilder extends ItemStack {
   public static final ItemBuilder FILLER_PANE = new ItemBuilder(MaterialGroup.GREY_GLASS_PANE.createItemStack());

   public ItemBuilder() {
      super(Material.AIR);
   }

   public ItemBuilder(Material material) {
      super(material);
   }

   public ItemBuilder(Material material, int i, short s) {
      super(material, i, s);
   }

   public ItemBuilder(ItemBuilder itemBuilder) {
      super(itemBuilder);
   }

   public ItemBuilder(ItemStack item) {
      super(item);
   }

   public ItemBuilder setDisplayName(String str) {
      ItemMeta itemMeta = this.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', str));
      this.setItemMeta(itemMeta);
      return this;
   }

   public ItemBuilder setLore(String[] strArray) {
      ItemMeta itemMeta = this.getItemMeta();
      itemMeta.setLore(Arrays.asList(strArray));
      this.setItemMeta(itemMeta);
      return this;
   }

   public ItemBuilder setLore(List list) {
      ItemMeta itemMeta = this.getItemMeta();
      itemMeta.setLore(list);
      this.setItemMeta(itemMeta);
      return this;
   }

   public ItemBuilder addItemFlags(ItemFlag[] itemFlagArray) {
      ItemMeta itemMeta = this.getItemMeta();
      itemMeta.addItemFlags(itemFlagArray);
      this.setItemMeta(itemMeta);
      return this;
   }

   public ItemBuilder copy() {
      return new ItemBuilder(this);
   }

   public ItemBuilder setSkullOwner(String str) {
      if (this.getType() != MaterialGroup.SKULL.createItemStack().getType()) {
         throw new IllegalArgumentException("Material has to be a skull item");
      }

      try {
         SkullMeta skullMeta = (SkullMeta)this.getItemMeta();
         skullMeta.setOwner(str);
         this.setItemMeta(skullMeta);
      } catch (Throwable ex) {
         Bukkit.getLogger().warning("[AAC] Could not receive skull owner for " + str);
      }

      return this;
   }

   public ItemStack clone() {
      return this.copy();
   }

}
