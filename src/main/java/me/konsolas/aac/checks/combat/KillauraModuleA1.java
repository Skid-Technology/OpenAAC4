package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.PacketMathUtils;
import com.comphenix.protocol.events.PacketEvent;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleA1 extends CombatCheckModule {
   private int violations;
   private long lastDamageTick;
   private long invalidGcdTicks;
   private float lastDeltaYaw;

   public KillauraModuleA1(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
      if (this.lastDamageTick + 10L <= ticks) {
         if (this.lastDamageTick + 20L <= ticks) {
            this.invalidGcdTicks = 0L;
         }
      } else {
         float yaw = Math.abs(PacketMathUtils.wrapYawDelta(event.getFrom().getYaw(), event.getTo().getYaw()));
         float f = this.lastDeltaYaw;
         this.lastDeltaYaw = yaw;
         if (!(yaw < 0.25) && !(f < 0.25)) {
            double d = PacketMathUtils.gcd((long)(yaw * 1.6777216E7F), (long)(f * 1.6777216E7F)) / 1.6777216E7;
            if ((yaw / d % 1.0 != 0.0 || f / d % 1.0 != 0.0) && this.invalidGcdTicks <= 30L) {
               this.invalidGcdTicks++;
            } else {
               if (this.invalidGcdTicks > 30L) {
                  this.violations++;
                  if (this.violations > 12) {
                     this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.HIGH, "t: " + this.invalidGcdTicks));
                  } else if (this.violations > 7) {
                     this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.MEDIUM, "t: " + this.invalidGcdTicks));
                  } else if (this.violations > 5) {
                     this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.LOW, "t: " + this.invalidGcdTicks));
                  }
               } else {
                  this.violations = Math.max(this.violations - 1, 0);
               }

               this.invalidGcdTicks = 0L;
            }
         }
      }
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
      this.lastDamageTick = ticks;
   }
}
