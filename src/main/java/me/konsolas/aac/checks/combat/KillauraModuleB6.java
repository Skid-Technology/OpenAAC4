package me.konsolas.aac.checks.combat;


import me.konsolas.aac.movement.EntityDimensions;
import me.konsolas.aac.util.BoundingBox;
import me.konsolas.aac.util.PacketMathUtils;
import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import java.util.function.Consumer;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class KillauraModuleB6 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer = new TimedThresholdBuffer(12L, 20);
   private long lastAttackTick;
   private long lastFlyingTick;
   private Entity entityValue;

   public KillauraModuleB6(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
      if (PacketMathUtils.isAttackPacket(event.getPacket())) {
         this.lastFlyingTick = 0L;
         this.lastAttackTick = ticks;
      } else if (event.getPacketType() == Client.ARM_ANIMATION && this.isCombatCheckReady()) {
         if (this.lastAttackTick == ticks) {
            this.lastAttackTick = 0L;
            return;
         }

         if (this.entityValue != null) {
            Vector vector = this.entityValue.getLocation().toVector();
            Entity entity = this.entityValue;
            double d = new BoundingBox(vector, EntityDimensions.forEntity(entity))
               .expand(-0.05, -0.15, -0.05)
               .rayTraceDistance(event.getPlayer().getEyeLocation().toVector(), event.getPlayer().getLocation().getDirection());
            if (!Double.isNaN(d) && d > 1.0 && d < 3.0) {
               this.lastFlyingTick = ticks;
            }
         }
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
      if (this.lastFlyingTick != 0L && this.buffer.record(ticks)) {
         this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.MEDIUM, ""));
         this.lastFlyingTick = 0L;
      }
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
      this.entityValue = event.getEntity();
   }
}
