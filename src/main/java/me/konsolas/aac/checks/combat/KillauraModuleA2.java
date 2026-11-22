package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.PacketMathUtils;
import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleA2 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer = new TimedThresholdBuffer(100L, 5);
   private boolean tracking;
   private int distinctYaws;
   private int repeatedYaws;
   private long trackingStartMillis;
   private Map yawCounts = new HashMap();
   private long lastAttackTick;

   public KillauraModuleA2(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
      if (event.getPacketType().equals(Client.LOOK)) {
         this.reset();
      } else if (PacketMathUtils.isAttackPacket(event.getPacket())) {
         this.lastAttackTick = ticks;
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
      long now = System.currentTimeMillis();
      if (this.tracking && now - this.trackingStartMillis > 2000L) {
         this.reset();
      }

      if (this.lastAttackTick + 10L > ticks) {
         if (this.tracking) {
            if (now - this.trackingStartMillis > 2000L) {
               this.reset();
            }
         } else {
            this.tracking = true;
            this.trackingStartMillis = System.currentTimeMillis();
         }

         if (this.yawCounts.size() == this.distinctYaws && this.repeatedYaws < 1 && this.distinctYaws > 32 && this.buffer.record(ticks)) {
            this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.MEDIUM, "f: " + this.distinctYaws));
         }

         float yaw = (float)PacketMathUtils.round(Math.abs(PacketMathUtils.wrapYawDelta(event.getFrom().getYaw(), event.getTo().getYaw())), 3);
         if (!this.yawCounts.containsKey(yaw)) {
            this.yawCounts.put(yaw, 1);
            this.distinctYaws++;
         } else {
            this.repeatedYaws++;
         }
      }
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }

   private void reset() {
      this.tracking = false;
      this.distinctYaws = 0;
      this.repeatedYaws = 0;
      this.yawCounts.clear();
   }
}
