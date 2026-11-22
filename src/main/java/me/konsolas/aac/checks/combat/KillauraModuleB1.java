package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.PacketMathUtils;
import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.events.PacketEvent;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleB1 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer = new TimedThresholdBuffer(20L, 10);
   private long lastUseEntityTick;
   private long lastArmAnimationTick;

   public KillauraModuleB1(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
      long now = System.currentTimeMillis() - this.lastUseEntityTick;
      if (PacketMathUtils.isAttackPacket(event.getPacket())) {
         if (now < 35L && this.lastArmAnimationTick + 4L < ticks && this.buffer.record(ticks)) {
            this.violationConsumer
               .accept(new CombatModuleViolation(ViolationTier.HIGH, "e: " + now + " l: " + this.buffer.getCount()));
         }
      } else if (PacketMathUtils.isMovementPacket(event.getPacketType())) {
         if (now < 5L) {
            this.lastArmAnimationTick = ticks;
         }

         this.lastUseEntityTick = System.currentTimeMillis();
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
