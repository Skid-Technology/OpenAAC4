package me.konsolas.aac.gui;


import me.konsolas.aac.AAC;
import me.konsolas.aac.util.ItemBuilder;
import me.konsolas.aac.util.MaterialGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import me.konsolas.aac.api.HackType;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class CheckMenuFactory {
   private static final Map DESCRIPTIONS = new CheckDescriptions();

   public static void open(Player player) {
      InventoryMenu inventoryMenu = new InventoryMenu(ChatColor.RED + "AAC " + ChatColor.DARK_GRAY + "GUI | Checks", 27);
      InventoryMenu inventoryMenu2 = inventoryMenu;
      String str = "combat";
      MenuItem menuItem = new DeferredOpenMenuItem(createCategoryMenu(str, inventoryMenu2))
         .setItemStack(
            new ItemBuilder(Material.IRON_SWORD)
               .setDisplayName("§7Combat checks")
               .setLore(
                  new String[]{
                     "§8Category contains:",
                     "",
                     "§eHitbox§7: Checks if a player is aiming at the target hitbox",
                     "§eCriticals§7: Checks for criticals whilst on the ground",
                     "§eFightSpeed§7: Checks for fast clicking / autoclicker",
                     "§eFastUse§7: Checks for consuming items too quickly",
                     "§eRegen§7: Checks regeneration speed",
                     "§eKillaura§7: Checks for basic killauras",
                     "§eFastBow§7: Checks for bow pullback speed",
                     "",
                     "§8HOTKEY: 1"
                  }
               )
               .addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES})
         )
         .setSlot(10)
         .setHotkey(0);
      inventoryMenu.addItem(menuItem);
      InventoryMenu inventoryMenu3 = inventoryMenu;
      String str2 = "movement";
      MenuItem menuItem2 = new DeferredOpenMenuItem(createCategoryMenu(str2, inventoryMenu3))
         .setItemStack(
            new ItemBuilder(Material.IRON_BOOTS)
               .setDisplayName("§7Movement checks")
               .setLore(
                  new String[]{
                     "§8Category contains:",
                     "",
                     "§eTimer§7: Checks if the player speeds up time",
                     "§ePhase§7: Checks if the player moves through a block",
                     "§eMove§7: Checks if the player tries to move incorrectly",
                     "§eNoFall§7: Checks if the player tries to avoid fall damage",
                     "§eDerp§7: Checks for impossible head rotations",
                     "",
                     "§8HOTKEY: 2"
                  }
               )
               .addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES})
         )
         .setSlot(12)
         .setHotkey(1);
      inventoryMenu.addItem(menuItem2);
      InventoryMenu inventoryMenu4 = inventoryMenu;
      String str3 = "world";
      MenuItem menuItem3 = new DeferredOpenMenuItem(createCategoryMenu(str3, inventoryMenu4))
         .setItemStack(
            new ItemBuilder(Material.DIRT)
               .setDisplayName("§7World checks")
               .setLore(
                  new String[]{
                     "§8Category contains:",
                     "",
                     "§eBadPackets§7: Checks for invalid packets",
                     "§eFastBreak§7: Checks block break speed",
                     "§eFastPlace§7: Checks block place speed",
                     "§eInteract§7: Checks for impossible interactions",
                     "",
                     "§8HOTKEY: 3"
                  }
               )
               .addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES})
         )
         .setSlot(14)
         .setHotkey(2);
      inventoryMenu.addItem(menuItem3);
      inventoryMenu.open(player);
   }

   public static InventoryMenu createCategoryMenu(String str, InventoryMenu inventoryMenu) {
      InventoryMenu inventoryMenu2 = new InventoryMenu(ChatColor.RED + "AAC " + ChatColor.DARK_GRAY + "GUI | " + WordUtils.capitalize(str) + " checks", 27);
      int value = 9;

      for (HackType hackType : HackType.values()) {
         if (hackType.toString().startsWith(str)) {
            int value2 = value;
            HackType hackType2 = hackType;
            InventoryMenu inventoryMenu3 = inventoryMenu2;
            addHackType(inventoryMenu3, hackType2, value2);
            value++;
         }
      }

      if (inventoryMenu != null) {
         OpenMenuItem openMenuItem = new OpenMenuItem(inventoryMenu);
         openMenuItem.setItemStack(new ItemBuilder(Material.BARRIER).setLore(new String[]{"§8HOTKEY: 9"}))
            .setDisplayName("Back to checks menu")
            .setSlot(22)
            .setHotkey(8);
         OpenMenuItem openMenuItem2 = openMenuItem;
         inventoryMenu2.addItem(openMenuItem2);
      }

      return inventoryMenu2;
   }

   private static void addHackType(InventoryMenu inventoryMenu, HackType hackType, int i) {
      AtomicBoolean atomicBoolean = new AtomicBoolean(AAC.getInstance().getCheckManager().isCheckEnabled(hackType));
      ContextClickMenuItem contextClickMenuItem = new ContextClickMenuItem(menuClick -> {
         if (atomicBoolean.get()) {
            AAC.getInstance().getCheckManager().disableCheck(hackType);
            atomicBoolean.set(false);
         } else {
            AAC.getInstance().getCheckManager().enableCheck(hackType);
            atomicBoolean.set(true);
         }

         ContextClickMenuItem item = menuClick.getItem();
         ItemBuilder builder = new ItemBuilder(
               atomicBoolean.get() ? MaterialGroup.GREEN_STAINED_CLAY.createItemStack() : MaterialGroup.GRAY_STAINED_CLAY.createItemStack()
            )
            .setDisplayName("§7" + WordUtils.capitalize(hackType.getName()));
         item.setItemStack(builder.setLore(buildCheckLore(hackType, atomicBoolean, i - 9)));
         inventoryMenu.refresh();
         inventoryMenu.open(menuClick.getPlayer());
      });
      ItemBuilder itemBuilder = new ItemBuilder(
            new ItemBuilder(atomicBoolean.get() ? MaterialGroup.GREEN_STAINED_CLAY.createItemStack() : MaterialGroup.GRAY_STAINED_CLAY.createItemStack())
         )
         .setDisplayName("§7" + WordUtils.capitalize(hackType.getName()));
      int value = i - 9;
      AtomicBoolean atomicBoolean2 = atomicBoolean;
      HackType hackType2 = hackType;
      MenuItem menuItem = contextClickMenuItem.setItemStack(itemBuilder.setLore(buildCheckLore(hackType2, atomicBoolean2, value))).setHotkey(i - 9).setSlot(i);
      MenuItem menuItem2 = menuItem;
      inventoryMenu.addItem(menuItem2);
   }

   private static List buildCheckLore(HackType hackType, AtomicBoolean atomicBoolean, int i) {
      ArrayList list = new ArrayList();
      String[] strArray = (String[])DESCRIPTIONS.get(hackType.toString());
      if (strArray != null) {
         list.add("§8Description:");
         list.add("");
         list.addAll(Arrays.asList(strArray));
         list.add("");
      }

      list.add("§8Status:");
      list.add("");
      StringBuilder sb = new StringBuilder().append("§7Currently this check is ");
      String str;
      if (atomicBoolean.get()) {
         str = "§2enabled";
      } else {
         str = "§4disabled";
      }

      list.add(sb.append(str).toString());
      list.add("§8HOTKEY: " + (i + 1));
      return list;
   }
}
