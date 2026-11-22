package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.PacketMathUtils;
import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.events.PacketEvent;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleA3 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer = new TimedThresholdBuffer(12L, 6);

   public KillauraModuleA3(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
      float yaw = event.getTo().getYaw();
      float yaw2 = PacketMathUtils.wrapYawDelta(event.getFrom().getYaw(), yaw);
      if (yaw2 > 0.1
         && (yaw2 % 0.5 == 0.0 || yaw % 1.0F == 0.0F)
         && event.getPlayer().getVehicle() == null
         && this.buffer.record(ticks)) {
         this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.HIGH, "dYaw: " + yaw2 + " to: " + yaw));
      }
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
