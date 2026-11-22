package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import java.util.Arrays;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleC4 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer = new TimedThresholdBuffer(100L, 7);
   private long lastInteractTick;
   private final long[] longArrayValue = new long[50];
   private int suspiciousCount;

   public KillauraModuleC4(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
      if (event.getPacketType() == Client.ARM_ANIMATION && this.isCombatCheckReady()) {
         long now = System.currentTimeMillis();
         long ticks2 = now - this.lastInteractTick;
         if (ticks2 < 160L) {
            this.longArrayValue[this.suspiciousCount++] = ticks2;
         }

         if (this.suspiciousCount >= this.longArrayValue.length) {
            double d = Arrays.stream(this.longArrayValue).average().orElse(0.0);
            double d2 = Math.abs(ticks2 - d);
            if (d2 < 10.0) {
               if (this.buffer.record(ticks)) {
                  this.violationConsumer
                     .accept(
                        new CombatModuleViolation(
                           this.buffer.getCount() > 30 ? ViolationTier.HIGH : ViolationTier.MEDIUM,
                           "a: " + d + " d: " + d2
                        )
                     );
               }
            } else {
               this.buffer.reset();
            }

            Arrays.fill(this.longArrayValue, 0L);
            this.suspiciousCount = 0;
         }

         this.lastInteractTick = now;
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
