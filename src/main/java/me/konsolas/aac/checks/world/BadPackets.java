package me.konsolas.aac.checks.world;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;
import java.util.Arrays;
import me.konsolas.aac.api.HackType;
import org.bukkit.entity.Player;

public class BadPackets extends Check {
   public BadPackets(CheckManager checkManager) {
      super(
         checkManager,
         HackType.BADPACKETS,
         BadPacketsPlayerData::new,
         Arrays.asList(Client.ABILITIES, Client.BLOCK_PLACE, Client.BLOCK_DIG, Client.ENTITY_ACTION, Client.FLYING),
         false,
         (player, data) -> true
      );
   }

   @Override
   public void onPacket(Player player, PlayerData playerData, PacketEvent event) {
      BadPacketsPlayerData data2 = (BadPacketsPlayerData)playerData.getCheckData(HackType.BADPACKETS);
      if (event.getPacketType() == Client.ABILITIES) {
         if (!player.getAllowFlight()) {
            String str = "check_abilities";
            if (this.getBooleanOption(str)) {
               String str2 = "sent invalid abilities packets";
               byte b = 50;
               Player player2 = player;
               if (this.addViolation(player2, b, str2)) {
                  event.setCancelled(true);
               }
            }
         }
      } else if (event.getPacketType() == Client.BLOCK_PLACE || event.getPacketType() == Client.USE_ITEM) {
         if (++data2.useToggleCount > this.getIntOption("max_use_toggle")) {
            String str3 = "toggled use item too quickly: " + data2.useToggleCount;
            byte b2 = 5;
            Player player3 = player;
            if (this.addViolation(player3, b2, str3)) {
               event.setCancelled(true);
            }
         }
      } else if (event.getPacketType() == Client.ENTITY_ACTION && event.getPacket().getPlayerActions().read(0) == PlayerAction.START_SNEAKING) {
         if (++data2.sneakToggleCount > this.getIntOption("max_sneak_toggle")) {
            String str4 = "toggled sneak too quickly: " + data2.sneakToggleCount;
            byte b3 = 5;
            Player player4 = player;
            if (this.addViolation(player4, b3, str4)) {
               event.setCancelled(true);
            }
         }
      }
   }

   @Override
   public void tick(long currentTick) {
      super.tick(currentTick);
      if (currentTick % 20L == 0L) {
         this.forAllPlayers((player, data) -> {
            ((BadPacketsPlayerData)data).useToggleCount = 0;
            ((BadPacketsPlayerData)data).sneakToggleCount = 0;
         });
      }
   }
}
