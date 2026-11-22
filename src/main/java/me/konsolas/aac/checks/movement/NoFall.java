package me.konsolas.aac.checks.movement;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.compat.PlayerStateAccess;
import me.konsolas.aac.compat.ProtocolVersion;
import me.konsolas.aac.movement.EntityDimensions;
import me.konsolas.aac.movement.MovementResolution;
import me.konsolas.aac.util.BoundingBox;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import java.util.Arrays;
import me.konsolas.aac.api.HackType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class NoFall extends Check {
   public NoFall(CheckManager checkManager) {
      super(checkManager, HackType.NOFALL, NoFallPlayerData::new, Arrays.asList(Client.POSITION, Client.POSITION_LOOK, Client.FLYING, Client.LOOK), true, (player, data) -> true);
   }

   @Override
   public void onPacket(Player player, PlayerData playerData, PacketEvent event) {
      NoFallPlayerData data = (NoFallPlayerData)playerData.getCheckData(HackType.NOFALL);
      data.packetGround = data.packetGround | (Boolean)event.getPacket().getBooleans().read(0);
      if (data.expectingFallDamage && !data.packetGround) {
         String str = "tried to avoid receiving fall damage";
         byte b = 1;
         Player player2 = player;
         if (this.addViolation(player2, b, str)) {
            event.getPacket().getBooleans().write(0, true);
         }

         data.expectingFallDamage = false;
      }
   }

   @Override
   public void onTeleport(Player player, PlayerData playerData, Location location) {
      NoFallPlayerData data = (NoFallPlayerData)playerData.getCheckData(HackType.NOFALL);
      data.maxFallY = 0.0;
   }

   @Override
   public MovementResolution move(Player player, PlayerData playerData, MovementTransition movementTransition) {
      NoFallPlayerData data;
      label31: {
         data = (NoFallPlayerData)playerData.getCheckData(HackType.NOFALL);
         double y = movementTransition.getTo().getY();
         if (!movementTransition.getFromGroundState().isOnGround()
            && !movementTransition.getToGroundState().isOnGround()
            && movementTransition.getTo().getBlock().getType() != Material.LADDER
            && movementTransition.getTo().getBlock().getType() != Material.VINE) {
            World world = player.getWorld();
            Vector vector = movementTransition.getTo().toVector();
            Player player2 = player;
            if (!PlayerStateAccess.checkWaterContact(
                  world, new BoundingBox(vector, EntityDimensions.forEntity(player2)), ProtocolVersion.getServerVersion()
               )
               .intersectsWater) {
               data.maxFallY = Math.max(data.maxFallY, y);
               break label31;
            }
         }

         data.maxFallY = 0.0;
      }

      if (data.packetGround != movementTransition.getToGroundState().isOnGround()) {
         if (data.packetGround) {
            if (data.maxFallY - movementTransition.getTo().getY() >= 1.5) {
               String str = "tried to avoid accumulating fall damage";
               byte b = 1;
               Player player3 = player;
               if (this.addViolation(player3, b, str)) {
                  player.setFallDistance((float)(data.maxFallY - movementTransition.getTo().getY()));
               }
            }
         } else if (player.getFallDistance() >= 4.0) {
            data.expectingFallDamage = true;
         }
      }

      data.packetGround = false;
      return null;
   }
}
