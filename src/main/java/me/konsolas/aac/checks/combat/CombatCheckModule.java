package me.konsolas.aac.checks.combat;


import com.comphenix.protocol.events.PacketEvent;
import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public abstract class CombatCheckModule {
   protected final Consumer<CombatModuleViolation> violationConsumer;
   private final KillauraPlayerData playerData;

   public CombatCheckModule(Consumer<CombatModuleViolation> consumer, KillauraPlayerData data) {
      this.violationConsumer = consumer;
      this.playerData = data;
   }

   protected boolean isCombatCheckReady() {
      return this.playerData.combatReadySupplier.getAsBoolean();
   }

   public abstract void onPacket(PacketEvent event, long ticks);

   public abstract void onPlayerMove(PlayerMoveEvent event, long ticks);

   public abstract void onEntityDamage(EntityDamageByEntityEvent event, long ticks);
}
