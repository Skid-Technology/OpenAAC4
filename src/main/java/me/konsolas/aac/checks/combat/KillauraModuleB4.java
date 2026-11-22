package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.PacketMathUtils;
import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.events.PacketEvent;
import java.util.Arrays;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleB4 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer = new TimedThresholdBuffer(200L, 4);
   private double[] doubleArrayValue = new double[20];
   private double[] doubleArrayValue2 = new double[20];
   private int hitStreak;
   private long lastAttackTick = 0L;

   public KillauraModuleB4(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
      float yaw = Math.abs(PacketMathUtils.wrapYawDelta(event.getFrom().getYaw(), event.getTo().getYaw()));
      float pitch = Math.abs(event.getTo().getPitch() - event.getFrom().getPitch());
      if (!(yaw > 25.0F)) {
         if (this.lastAttackTick + 10L >= ticks) {
            this.doubleArrayValue[this.hitStreak] = yaw;
            this.doubleArrayValue2[this.hitStreak] = pitch;
            this.hitStreak++;
            if (this.hitStreak >= 20) {
               this.hitStreak = 0;
               long ticks2 = this.doubleArrayValue.length - Arrays.stream(this.doubleArrayValue).distinct().count();
               long ticks3 = this.doubleArrayValue2.length - Arrays.stream(this.doubleArrayValue2).distinct().count();
               if (ticks2 != 0L || ticks3 != 0L) {
                  this.buffer.reset();
               } else if (this.buffer.record(ticks)) {
                  this.violationConsumer
                     .accept(
                        new CombatModuleViolation(
                           this.buffer.getCount() > 6 ? ViolationTier.MEDIUM : ViolationTier.LOW, ""
                        )
                     );
               }
            }
         }
      }
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
      this.lastAttackTick = ticks;
   }
}
