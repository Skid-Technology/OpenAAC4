package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.events.PacketEvent;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class KillauraModuleA4 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer = new TimedThresholdBuffer(20L, 3);

   public KillauraModuleA4(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
      Vector x = new Vector(event.getTo().getX() - event.getFrom().getX(), 0.0, event.getTo().getZ() - event.getFrom().getZ());
      Vector yaw = new Vector(event.getTo().getYaw() - event.getFrom().getYaw(), 0.0F, event.getTo().getYaw() - event.getFrom().getYaw());
      double d = x.distance(yaw);
      double d2 = x.length();
      if (d > 100000.0 && d2 > 0.2 && d2 < 1.0 && this.buffer.record(ticks)) {
         this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.HIGH, "d: " + d));
      }
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
