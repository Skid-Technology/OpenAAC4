package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import java.util.Arrays;
import java.util.LongSummaryStatistics;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleC5 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer;
   private long lastTick;
   private long tickDiff;
   private long[] longArrayValue;
   private int streak;

   public KillauraModuleC5(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
      this.buffer = new TimedThresholdBuffer(100L, 15);
      this.longArrayValue = new long[20];
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
      if (event.getPacketType() == Client.ARM_ANIMATION && this.isCombatCheckReady()) {
         long now = System.currentTimeMillis();
         long ticks2 = now - this.lastTick;
         if (ticks2 > 2L && this.streak < 20 && ticks2 < 1000L) {
            this.longArrayValue[this.streak++] = ticks2;
         } else if (this.streak >= 20) {
            LongSummaryStatistics longSummaryStatistics = Arrays.stream(this.longArrayValue).summaryStatistics();
            long ticks3 = longSummaryStatistics.getMax() - longSummaryStatistics.getMin();
            if (longSummaryStatistics.getAverage() > 110.0 && (ticks3 < 65L || Math.abs(ticks3 - this.tickDiff) < 3L)) {
               if (this.buffer.record(ticks)) {
                  this.violationConsumer
                     .accept(
                        new CombatModuleViolation(
                           this.buffer.getCount() >= 25 ? ViolationTier.HIGH : ViolationTier.MEDIUM,
                           "r: " + ticks3 + " lr: " + this.tickDiff + " a: " + longSummaryStatistics.getAverage()
                        )
                     );
               }
            } else {
               this.buffer.decrement(1);
            }

            this.tickDiff = ticks3;
            this.streak = 0;
         }

         this.lastTick = now;
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
