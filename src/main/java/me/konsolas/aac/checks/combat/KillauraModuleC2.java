package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.PacketMathUtils;
import me.konsolas.aac.util.RollingDoubleWindow;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import java.util.DoubleSummaryStatistics;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleC2 extends CombatCheckModule {
   private int consistentCount;
   private int inconsistentCount;
   private int sampleCount;
   private RollingDoubleWindow rollingDoubleWindowValue = new RollingDoubleWindow(5);

   public KillauraModuleC2(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
      if (event.getPacketType() == Client.ARM_ANIMATION && this.isCombatCheckReady()) {
         this.consistentCount++;
      } else if (PacketMathUtils.isMovementPacket(event.getPacketType()) && ++this.inconsistentCount == 20) {
         if (this.consistentCount > 0) {
            this.rollingDoubleWindowValue.addSample(this.consistentCount);
            DoubleSummaryStatistics doubleSummaryStatistics = this.rollingDoubleWindowValue.stream().summaryStatistics();
            if (!(doubleSummaryStatistics.getAverage() >= 8.0) || !(doubleSummaryStatistics.getAverage() < 17.0) || doubleSummaryStatistics.getMax() != doubleSummaryStatistics.getMin()) {
               this.sampleCount = Math.max(this.sampleCount - 1, 0);
            } else if ((this.sampleCount += 2) > 12) {
               this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.MEDIUM, "v: " + this.sampleCount + " a: " + doubleSummaryStatistics.getAverage()));
            } else if (this.sampleCount > 7) {
               this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.LOW, "v: " + this.sampleCount + " a: " + doubleSummaryStatistics.getAverage()));
            }
         }

         this.inconsistentCount = 0;
         this.consistentCount = 0;
         if (this.rollingDoubleWindowValue.isFilled()) {
            this.rollingDoubleWindowValue.reset();
         }
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
