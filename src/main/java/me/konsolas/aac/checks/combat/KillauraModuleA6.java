package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.PacketMathUtils;
import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.events.PacketEvent;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleA6 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer = new TimedThresholdBuffer(100L, 10);
   private float lastDeltaYaw;
   private float lastDeltaPitch;

   public KillauraModuleA6(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
      float pitch = event.getTo().getPitch() - event.getFrom().getPitch();
      float f = this.lastDeltaYaw;
      this.lastDeltaYaw = pitch;
      float yaw = PacketMathUtils.wrapYawDelta(event.getFrom().getYaw(), event.getTo().getYaw());
      float f2 = this.lastDeltaPitch;
      this.lastDeltaPitch = yaw;
      float f3 = yaw - f2;
      float f4 = pitch - f;
      if (f3 != 0.0F || f4 != 0.0F || event.getPlayer().getVehicle() != null || !(Math.abs(event.getTo().getPitch()) < 80.0F) || !(Math.abs(yaw) > 0.1)) {
         this.buffer.decrement(2);
      } else if (this.buffer.record(ticks)) {
         this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.MEDIUM, "dYaw: " + yaw));
      }
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
