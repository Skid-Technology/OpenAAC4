package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.PacketMathUtils;
import com.comphenix.protocol.events.PacketEvent;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleA5 extends CombatCheckModule {
   private int steadyTicks;
   private float lastDeltaYaw;

   public KillauraModuleA5(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
      float yaw = Math.abs(PacketMathUtils.wrapYawDelta(event.getFrom().getYaw(), event.getTo().getYaw()));
      float pitch = event.getTo().getPitch() - event.getFrom().getPitch();
      float f = this.lastDeltaYaw;
      this.lastDeltaYaw = pitch;
      float f2 = pitch - f;
      if (f2 == 0.0F && event.getPlayer().getVehicle() == null && Math.abs(event.getTo().getPitch()) < 80.0F && yaw > 1.8) {
         if (this.steadyTicks++ > 30) {
            this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.HIGH, "dYaw: " + yaw));
         }
      } else {
         this.steadyTicks = Math.max(this.steadyTicks - 4, 0);
      }
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
