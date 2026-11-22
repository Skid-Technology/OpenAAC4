package me.konsolas.aac.checks.world;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.compat.PlayerStateAccess;
import me.konsolas.aac.util.MaterialGroup;
import me.konsolas.aac.api.HackType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class FastPlace extends Check {
   private static final ItemStack itemStackValue = MaterialGroup.BONE_MEAL.createItemStack();

   public FastPlace(CheckManager checkManager) {
      super(checkManager, HackType.FASTPLACE, () -> new FastPlacePlayerData(null), null, true, (player, data) -> true);
   }

   @Override
   public void onBlockPlace(Player player, PlayerData playerData, BlockPlaceEvent event) {
      FastPlacePlayerData data = (FastPlacePlayerData)playerData.getCheckData(HackType.FASTPLACE);

      for (ItemStack item : PlayerStateAccess.getHeldItems(event.getPlayer())) {
         if (item == null || item.getType() == Material.FLINT_AND_STEEL || item.isSimilar(itemStackValue)) {
            return;
         }
      }

      long now = System.currentTimeMillis();
      long ticks = now - data.lastPlaceMillis;
      long ticks2 = data.lastIntervalMillis;
      data.lastPlaceMillis = now;
      data.lastIntervalMillis = ticks;
      String str = "min_delay";
      long ticks3 = this.getLongOption(str);
      if (ticks < ticks3 && ticks2 < ticks3) {
         String str2 = "insufficient delay placing " + event.getBlockPlaced().getType() + ": " + ticks;
         byte b = 1;
         Player player2 = player;
         if (this.addViolation(player2, b, str2)) {
            event.setCancelled(true);
         }
      }
   }
}
