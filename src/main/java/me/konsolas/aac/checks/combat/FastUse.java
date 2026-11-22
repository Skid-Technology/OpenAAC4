package me.konsolas.aac.checks.combat;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.api.HackType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class FastUse extends Check {
   public FastUse(CheckManager checkManager) {
      super(checkManager, HackType.FASTUSE, () -> new FastUsePlayerData(null), null, true, (player, data) -> true);
   }

   @Override
   public void onPlayerInteract(Player player, PlayerData playerData, PlayerInteractEvent event) {
      FastUsePlayerData data = (FastUsePlayerData)playerData.getCheckData(HackType.FASTUSE);
      if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         data.lastInteractMillis = System.currentTimeMillis();
      }
   }

   @Override
   public void onPlayerItemConsume(Player player, PlayerData playerData, PlayerItemConsumeEvent event) {
      FastUsePlayerData data = (FastUsePlayerData)playerData.getCheckData(HackType.FASTUSE);
      Player player2 = player;
      if (this.shouldMitigate(player2)) {
         event.setCancelled(true);
      } else {
         long now = System.currentTimeMillis() - data.lastInteractMillis;
         String str = "min_consumption_time";
         if (now < this.getLongOption(str)) {
            String str2 = "consumed " + event.getItem().getType().name() + " too quickly (" + now + " ms)";
            byte b = 1;
            Player player3 = player;
            if (this.addViolation(player3, b, str2)) {
               event.setCancelled(true);
               player.getInventory().setHeldItemSlot((player.getInventory().getHeldItemSlot() + 1) % 9);
            }
         }
      }
   }
}
