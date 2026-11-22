package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.PacketMathUtils;
import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleB2 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer = new TimedThresholdBuffer(20L, 4);
   private int postSwingTicks;
   private int swingCount;

   public KillauraModuleB2(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
      if (PacketMathUtils.isAttackPacket(event.getPacket())) {
         this.swingCount++;
      } else if (event.getPacketType() == Client.ARM_ANIMATION) {
         this.postSwingTicks++;
      } else if (PacketMathUtils.isMovementPacket(event.getPacketType())) {
         if (this.swingCount > this.postSwingTicks && this.buffer.record(ticks)) {
            this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.HIGH, "a: " + this.swingCount + " s: " + this.postSwingTicks));
         }

         this.swingCount = 0;
         this.postSwingTicks = 0;
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
