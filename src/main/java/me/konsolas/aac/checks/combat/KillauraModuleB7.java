package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.PacketMathUtils;
import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.events.PacketEvent;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleB7 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer = new TimedThresholdBuffer(15L, 12);
   private float lastYaw;
   private float lastPitch;

   public KillauraModuleB7(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
      float pitch = event.getTo().getPitch() - event.getFrom().getPitch();
      float f = this.lastYaw;
      this.lastYaw = pitch;
      float yaw = PacketMathUtils.wrapYawDelta(event.getFrom().getYaw(), event.getTo().getYaw());
      float f2 = this.lastPitch;
      this.lastPitch = yaw;
      if (Math.abs(pitch) > 10.0 && Math.abs(f) > 10.0 && Math.abs(pitch + f) < 1.0 && this.buffer.record(ticks)) {
         this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.HIGH, "p: " + pitch + " s: " + (pitch + f)));
      }

      if (Math.abs(yaw) > 10.0 && Math.abs(f2) > 10.0 && Math.abs(yaw + f2) < 1.0 && this.buffer.record(ticks)) {
         this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.HIGH, "y: " + yaw + " s: " + (yaw + f2)));
      }
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
