package me.konsolas.aac.checks.world;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.compat.BlockBreakAccess;
import me.konsolas.aac.api.HackType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

public class FastBreak extends Check {
   public FastBreak(CheckManager checkManager) {
      super(checkManager, HackType.FASTBREAK, FastBreakPlayerData::new, null, true, (player, data) -> true);
   }

   @Override
   public void onBlockDamage(Player player, PlayerData playerData, BlockDamageEvent event) {
      FastBreakPlayerData data = (FastBreakPlayerData)playerData.getCheckData(HackType.FASTBREAK);
      data.targetBlockPos = event.getBlock().getLocation().toVector();
      data.damageStartMillis = event.getInstaBreak() ? 0L : System.currentTimeMillis();
      String str = "check_delay";
      if (this.getBooleanOption(str) && BlockBreakAccess.getHasteAmplifier(player) == 0) {
         long now = System.currentTimeMillis() - data.lastBreakMillis;
         if (now < 100L && BlockBreakAccess.getBreakTicks(event.getBlock(), player) > 1L) {
            String str2 = "interacted too quickly after breaking a block: " + now + "ms";
            byte b = 2;
            Player player2 = player;
            if (this.addViolation(player2, b, str2)) {
               event.setCancelled(true);
            }
         }
      }
   }

   @Override
   public void onBlockBreak(Player player, PlayerData playerData, BlockBreakEvent event) {
      FastBreakPlayerData data = (FastBreakPlayerData)playerData.getCheckData(HackType.FASTBREAK);
      Player player2 = player;
      if (this.shouldMitigate(player2)) {
         event.setCancelled(true);
      }

      if (!event.getBlock().getLocation().toVector().equals(data.targetBlockPos)) {
         String str = "broke the wrong block";
         byte b = 1;
         Player player3 = player;
         if (this.addViolation(player3, b, str)) {
            event.setCancelled(true);
         }
      }

      long now = (System.currentTimeMillis() - data.damageStartMillis) / 50L;
      long ticks = BlockBreakAccess.getBreakTicks(event.getBlock(), player);
      if (ticks != 0L) {
         String str2 = "tolerance";
         if (now + this.getLongOption(str2) < ticks) {
            String str3 = "tried to break a block too quickly: " + now + "/" + ticks + " ticks";
            byte b2 = 1;
            Player player4 = player;
            if (this.addViolation(player4, b2, str3)) {
               event.setCancelled(true);
            }
         }
      }
   }
}
