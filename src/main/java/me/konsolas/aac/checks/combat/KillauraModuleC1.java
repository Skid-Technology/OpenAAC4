package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.PacketMathUtils;
import me.konsolas.aac.util.RollingDoubleWindow;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleC1 extends CombatCheckModule {
   private final RollingDoubleWindow rollingDoubleWindowValue = new RollingDoubleWindow(5);
   private int packetCount;
   private int flagCount;
   private double lastYawDiff;

   public KillauraModuleC1(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
      if (event.getPacketType() == Client.ARM_ANIMATION && this.isCombatCheckReady()) {
         this.packetCount++;
      } else if (PacketMathUtils.isMovementPacket(event.getPacketType()) && ++this.flagCount == 20 && this.packetCount > 0) {
         this.rollingDoubleWindowValue.addSample(this.packetCount);
         double d = this.rollingDoubleWindowValue.stream().average().orElse(0.0);
         if (d >= 9.0) {
            if (Math.round(d) != d && Math.round(d) != d - 0.5) {
               this.lastYawDiff = Math.max(0.0, this.lastYawDiff - 4.0);
            } else if ((this.lastYawDiff += 4.0) > 40.0) {
               this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.MEDIUM, "a: " + d));
            }
         } else {
            this.lastYawDiff = Math.max(0.0, this.lastYawDiff - 1.0);
         }

         if (this.rollingDoubleWindowValue.isFilled()) {
            this.rollingDoubleWindowValue.reset();
         }

         this.packetCount = 0;
         this.flagCount = 0;
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
