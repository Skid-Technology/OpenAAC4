package me.konsolas.aac.checks.combat;


import me.konsolas.aac.util.PacketMathUtils;
import me.konsolas.aac.util.TimedThresholdBuffer;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillauraModuleB3 extends CombatCheckModule {
   private final TimedThresholdBuffer buffer = new TimedThresholdBuffer(30L, 3);
   private long lastEntityActionTick;
   private boolean hadSprintAction;

   public KillauraModuleB3(Consumer consumer, KillauraPlayerData data) {
      super(consumer, data);
   }

   @Override
   public void onPacket(PacketEvent event, long ticks) {
      if (event.getPacketType() == Client.ENTITY_ACTION) {
         if (event.getPacket().getPlayerActions().read(0) == PlayerAction.START_SPRINTING) {
            if (this.hadSprintAction) {
               if (ticks <= this.lastEntityActionTick + 1L) {
                  if (this.buffer.record(ticks)) {
                     this.violationConsumer.accept(new CombatModuleViolation(ViolationTier.HIGH, ""));
                  }
               } else {
                  this.hadSprintAction = false;
               }
            } else {
               this.hadSprintAction = true;
               this.lastEntityActionTick = ticks;
            }
         }
      } else if (PacketMathUtils.isMovementPacket(event.getPacketType())) {
         this.hadSprintAction = false;
      }
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event, long ticks) {
   }

   @Override
   public void onEntityDamage(EntityDamageByEntityEvent event, long ticks) {
   }
}
