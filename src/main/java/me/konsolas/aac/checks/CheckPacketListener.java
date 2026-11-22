package me.konsolas.aac.checks;


import me.konsolas.aac.util.PlayerDigTypeSwitch;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import java.util.Optional;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class CheckPacketListener extends PacketAdapter {
   public final CheckManager checkManagerValue;

   public CheckPacketListener(CheckManager checkManager, Set<PacketType> set) {
      super(CheckManager.getPluginInstance(checkManager), ListenerPriority.HIGH, set);
      this.checkManagerValue = checkManager;
   }

   public void onPacketReceiving(PacketEvent event) {
      if (!event.isCancelled()) {
         this.onPacket(event);
      }
   }

   public void onPacketSending(PacketEvent event) {
      if (!event.isCancelled()) {
         PacketEvent event2 = event;
         this.onPacket(event2);
      }
   }

   private void onPacket(PacketEvent event) {
      CheckManager checkManager = this.checkManagerValue;
      Player player = event.getPlayer();
      Optional optional = checkManager.getPlayerData(player);
      if (optional.isPresent()) {
         if (event.getPacketType() == Client.BLOCK_DIG) {
            switch (PlayerDigTypeSwitch.ordinalSwitch[((PlayerDigType)event.getPacket().getPlayerDigTypes().read(0)).ordinal()]) {
               case 1:
                  ((PlayerData)optional.get()).getMovementState().blockingOrDigging = true;
                  break;
               case 2:
               case 3:
                  ((PlayerData)optional.get()).getMovementState().lastDigTick = CheckManager.getTickCounter(this.checkManagerValue).get();
                  ((PlayerData)optional.get()).getMovementState().blockingOrDigging = false;
                  ((PlayerData)optional.get()).getMovementState().lastDigPosition = (BlockPosition)event.getPacket().getBlockPositionModifier().read(0);
            }
         }

         if (event.getPacketType() == Server.ENTITY_VELOCITY) {
            int value = (Integer)event.getPacket().getIntegers().read(0);
            if (value == event.getPlayer().getEntityId()) {
               int value2 = (Integer)event.getPacket().getIntegers().read(1);
               int value3 = (Integer)event.getPacket().getIntegers().read(2);
               int value4 = (Integer)event.getPacket().getIntegers().read(3);
               Vector vector = new Vector(value2 / 8000.0, value3 / 8000.0, value4 / 8000.0);
               ((PlayerData)optional.get()).getMovementState().pendingKnockback = vector;
               ((PlayerData)optional.get()).getMovementState().lastKnockbackMillis = System.currentTimeMillis();
               ((PlayerData)optional.get()).getMovementState().knockbackTicksRemaining = 3;

               for (Check check : CheckManager.getEnabledChecksMap(this.checkManagerValue).values()) {
                  check.onVelocity(event.getPlayer(), (PlayerData)optional.get(), vector.clone());
               }
            }
         }

         for (Check check2 : CheckManager.getEnabledChecksMap(this.checkManagerValue).values()) {
            if (check2.getDependencies().contains(event.getPacketType())) {
               check2.onPacket(event.getPlayer(), (PlayerData)optional.get(), event);
            }
         }
      }
   }
}
