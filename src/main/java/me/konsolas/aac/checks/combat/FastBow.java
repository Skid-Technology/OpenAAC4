package me.konsolas.aac.checks.combat;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.compat.ProtocolVersion;
import me.konsolas.aac.api.HackType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class FastBow extends Check {
   public FastBow(CheckManager checkManager) {
      super(checkManager, HackType.FASTBOW, FastBowPlayerData::new, null, true, (player, data) -> true);
   }

   private long getExpectedDrawTimeMillis(double d) {
      return (long)(100.0 + 950.0 * d);
   }

   @Override
   public void onPlayerInteract(Player player, PlayerData playerData, PlayerInteractEvent event) {
      Killaura.unusedFlag();
      if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         FastBowPlayerData data = (FastBowPlayerData)playerData.getCheckData(HackType.FASTBOW);
         data.lastInteractMillis = System.currentTimeMillis();
      }
   }

   @Override
   public void onEntityShootBow(Player player, PlayerData playerData, EntityShootBowEvent event) {
      Killaura.unusedFlag();
      FastBowPlayerData data = (FastBowPlayerData)playerData.getCheckData(HackType.FASTBOW);
      if (!ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_14) || event.getBow() == null || event.getBow().getType() != Material.CROSSBOW) {
         Player player2 = player;
         if (this.shouldMitigate(player2)) {
            event.setCancelled(true);
         } else {
            double d = event.getForce();
            long ticks = this.getExpectedDrawTimeMillis(d);
            String str = "lenience";
            long ticks2 = ticks - this.getLongOption(str);
            long now = System.currentTimeMillis() - data.lastInteractMillis;
            if (now >= ticks2) {
               data.lastInteractMillis = System.currentTimeMillis();
            } else if (ticks2 > 200L) {
               int value = (int)(1L + Math.abs((now - ticks2) / 90L));
               String str2 = "shooting arrows too quickly (" + now + "/" + ticks2 + " ms)";
               int value2 = value;
               Player player3 = player;
               if (this.addViolation(player3, value2, str2)) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }
}
