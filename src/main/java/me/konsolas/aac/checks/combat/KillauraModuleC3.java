package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleC3 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer = new TimedThresholdBuffer(100L, 25);
   private long lastBlockPlaceTick;

   public KillauraModuleC3(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
      if (event.getPacketType() == Client.ARM_ANIMATION && this.isCombatCheckReady()) {
         long now = System.currentTimeMillis();
         long ticks2 = now - this.lastBlockPlaceTick;
         if (ticks2 > 0L && ticks2 < 200L) {
            double d = 1000.0 / ticks2;
            if (d % 0.5 != 0.0 && d % 1.0 != 0.0) {
               this.buffer.decrement(1);
            } else if (this.buffer.record(ticks, 2)) {
               this.violationConsumer
                  .accept(new CombatModuleViolation(ViolationTier.MEDIUM, "v: " + this.buffer.getCount() + " c: " + d));
            }
         }

         this.lastBlockPlaceTick = now;
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
