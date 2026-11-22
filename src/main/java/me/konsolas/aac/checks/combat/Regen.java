package me.konsolas.aac.checks.combat;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.compat.ProtocolVersion;
import java.util.Collections;
import me.konsolas.aac.api.HackType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class Regen extends Check {
   public Regen(CheckManager checkManager) {
      super(checkManager, HackType.REGEN, RegenPlayerData::new, Collections.emptyList(), true, (player, data) -> true);
   }

   @Override
   public void onEntityRegainHealth(Player player, PlayerData playerData, EntityRegainHealthEvent event) {
      Killaura.unusedFlag();
      RegenPlayerData data = (RegenPlayerData)playerData.getCheckData(HackType.REGEN);
      if (event.getRegainReason() == RegainReason.SATIATED) {
         long now = System.currentTimeMillis() - data.lastHealMillis;
         if (!ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_9)) {
            String str = "min_regen_delay";
            if (now < this.getLongOption(str)) {
               String str2 = "regenerated health too quickly (" + now + " ms)";
               byte b = 1;
               Player player2 = player;
               if (this.addViolation(player2, b, str2)) {
                  event.setCancelled(true);
               }
            }
         } else {
            String str3 = "min_satiated_delay";
            if (now < this.getLongOption(str3)) {
               String str4 = "regenerated health too quickly (" + now + "ms)";
               byte b2 = 1;
               Player player3 = player;
               if (this.addViolation(player3, b2, str4)) {
                  event.setCancelled(true);
               }
            } else if (player.getFoodLevel() < 20) {
               String str5 = "min_regen_delay";
               if (now < this.getLongOption(str5)) {
                  String str6 = "regenerated health too quickly (" + now + "ms)";
                  byte b3 = 1;
                  Player player4 = player;
                  if (this.addViolation(player4, b3, str6)) {
                     event.setCancelled(true);
                  }
               }
            }
         }
      }
   }
}
