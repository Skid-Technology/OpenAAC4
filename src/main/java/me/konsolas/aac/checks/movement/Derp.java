package me.konsolas.aac.checks.movement;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.CheckPlayerData;
import me.konsolas.aac.checks.PlayerData;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import java.util.Arrays;
import me.konsolas.aac.api.HackType;
import org.bukkit.entity.Player;

public class Derp extends Check {
   public Derp(CheckManager checkManager) {
      super(checkManager, HackType.DERP, CheckPlayerData::new, Arrays.asList(Client.LOOK, Client.POSITION_LOOK), false, (player, data) -> true);
   }

   @Override
   public void onPacket(Player player, PlayerData playerData, PacketEvent event) {
      if (event.getPacketType() == Client.LOOK || event.getPacketType() == Client.POSITION_LOOK) {
         float f = (Float)event.getPacket().getFloat().read(1);
         if (f > 90.1F || f < -90.1F) {
            event.setCancelled(true);
            Player player2 = event.getPlayer();
            String str = "moved with an illegal pitch (" + f + ")";
            byte b = 1;
            Player player3 = player2;
            this.addViolation(player3, b, str);
         }
      }
   }
}
