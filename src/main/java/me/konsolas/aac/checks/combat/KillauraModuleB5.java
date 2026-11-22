package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.PacketMathUtils;
import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.events.PacketEvent;
import java.util.Arrays;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleB5 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer = new TimedThresholdBuffer(20L, 2);
   private final double[] yawDeltas = new double[5];
   private final double[] pitchDeltas = new double[5];
   private int sampleIndex;

   public KillauraModuleB5(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
      float yaw = Math.abs(PacketMathUtils.wrapYawDelta(event.getFrom().getYaw(), event.getTo().getYaw()));
      float pitch = Math.abs(event.getTo().getPitch() - event.getFrom().getPitch());
      this.yawDeltas[this.sampleIndex] = yaw;
      this.pitchDeltas[this.sampleIndex] = pitch;
      if (this.sampleIndex >= 5) {
         this.sampleIndex = 0;
         long ticks2 = Arrays.stream(this.yawDeltas).filter(v -> v > 10.0).count()
            + Arrays.stream(this.pitchDeltas).filter(v -> v > 7.0).count();
         double d = Arrays.stream(this.yawDeltas).average().orElse(0.0);
         double d2 = Arrays.stream(this.pitchDeltas).average().orElse(0.0);
         if (d2 >= 0.0 && d >= 5.6 && ticks2 >= 3L && ticks2 < 6L) {
            if (this.buffer.record(ticks)) {
               this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.HIGH, d2 + " / " + d));
            } else {
               this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.LOW, d2 + " / " + d));
            }
         }
      }
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
