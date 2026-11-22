package me.konsolas.aac.gui;


import me.konsolas.aac.AAC;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.command.CommandFormatter;
import me.konsolas.aac.punishment.DefaultViolationWeights;
import me.konsolas.aac.punishment.PlayerViolationSummary;
import me.konsolas.aac.util.ItemBuilder;
import me.konsolas.aac.util.MapValueSorter;
import me.konsolas.aac.util.MaterialGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import me.konsolas.aac.api.HackType;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class ViolationMenuFactory {
   private static final Map<HackType, Double> VIOLATION_WEIGHTS = Collections.unmodifiableMap(new DefaultViolationWeights(HackType.class));

   public static void open(Player player) {
      ArrayList<PlayerViolationSummary> list = new ArrayList<>();

      for (Player player2 : Bukkit.getOnlinePlayers()) {
         int value = 0;
         HashMap<String, Integer> map = new HashMap<>();

         for (HackType hackType : HackType.values()) {
            if (AAC.getInstance().getCheckManager().isCheckEnabled(hackType)) {
               CheckManager checkManager = AAC.getInstance().getCheckManager();
               Player player3 = player2;
               Optional<PlayerData> optional = checkManager.getPlayerData(player3);
               if (optional.isPresent()) {
                  int value2 = optional.get().getCheckData(hackType).violationLevel;
                  value2 = (int)(VIOLATION_WEIGHTS.get(hackType) * value2);
                  if (value2 > 0) {
                     value += value2;
                     map.put(WordUtils.capitalize(hackType.getName()), value2);
                  }
               }
            }
         }

         Map<String, Integer> map2 = MapValueSorter.sortByValue(map);
         ArrayList<String> list2 = new ArrayList<>();
         if (map2.size() > 0) {
            list2.add("§8Violation breakdown");
            list2.add("");

            for (Entry<String, Integer> entry : map2.entrySet()) {
               list2.add("§7" + entry.getKey() + ": §c" + entry.getValue() + " VL");
            }

            list2.add("");
            list2.add("§rClick to switch to §cSPECTATOR");
            list2.add("§rand §cteleport silently§r to the player");
         }

         list.add(new PlayerViolationSummary(player2, value, list2, null));
      }

      list.sort((a, b) -> Integer.compare(b.getTotalViolationLevel(), a.getTotalViolationLevel()));
      ArrayList<MenuItem> list3 = new ArrayList<>();

      for (PlayerViolationSummary playerViolationSummary : list) {
         CallbackMenuItem callbackMenuItem = new CallbackMenuItem(player4 -> {
            if (player4.getGameMode() != GameMode.SPECTATOR) {
               player4.setGameMode(GameMode.SPECTATOR);
               CommandFormatter.sendInfo(player4, "Updated gamemode to {SPECTATOR}");
            }

            player4.teleport(playerViolationSummary.getPlayer().getLocation().add(0.0, 1.0, 0.0));
            CommandFormatter.sendInfo(player4, "Teleported to {" + playerViolationSummary.getPlayer().getName() + "}");
         });
         ItemBuilder itemBuilder = new ItemBuilder(MaterialGroup.SKULL.createItemStack().getType(), 1, (short)3);
         String str = playerViolationSummary.getPlayer().getName();
         list3.add(
            callbackMenuItem.setItemStack(
               itemBuilder.setSkullOwner(str)
                  .setDisplayName("§e" + playerViolationSummary.getPlayer().getName() + " §7[§c" + playerViolationSummary.getTotalViolationLevel() + " VL§7]")
                  .addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES})
                  .setLore(playerViolationSummary.getBreakdown())
            )
         );
      }

      StaticMenuItem staticMenuItem = new StaticMenuItem();
      ItemBuilder itemBuilder2 = new ItemBuilder(MaterialGroup.SKULL.createItemStack().getType(), 1, (short)3);
      String str2 = "MHF_ArrowLeft";
      MenuItem menuItem = staticMenuItem.setItemStack(itemBuilder2.setSkullOwner(str2).setLore(new String[]{"§8HOTKEY: 1"})).setDisplayName("Back").setSlot(18);
      StaticMenuItem staticMenuItem2 = new StaticMenuItem();
      itemBuilder2 = new ItemBuilder(MaterialGroup.SKULL.createItemStack().getType(), 1, (short)3);
      String str3 = "MHF_ArrowRight";
      MenuItem menuItem2 = staticMenuItem2.setItemStack(itemBuilder2.setSkullOwner(str3).setLore(new String[]{"§8HOTKEY: 2"})).setDisplayName("Next").setSlot(26);
      String str4 = ChatColor.RED + "AAC " + ChatColor.DARK_GRAY + "GUI | Violations";
      Object obj = null;
      MenuItem menuItem3 = menuItem2;
      MenuItem menuItem4 = menuItem;
      ArrayList<MenuItem> list4 = list3;
      byte b2 = 18;
      byte b3 = 27;
      String str5 = str4;
      new PaginatedInventoryMenu(str5, b3, b2, list4, menuItem4, menuItem3, (InventoryMenu)obj).open(player);
   }
}
