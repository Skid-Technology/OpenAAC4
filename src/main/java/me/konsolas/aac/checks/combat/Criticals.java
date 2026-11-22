package me.konsolas.aac.checks.combat;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.CheckPlayerData;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.checks.movement.MovementTransition;
import me.konsolas.aac.compat.CollisionBoxAccess;
import me.konsolas.aac.movement.EntityDimensions;
import me.konsolas.aac.movement.MovementResolution;
import me.konsolas.aac.util.BoundingBox;
import me.konsolas.aac.util.EnvironmentBlockChecks;
import me.konsolas.aac.util.MaterialGroup;
import me.konsolas.aac.util.RelativeBlockAccess;
import me.konsolas.aac.api.HackType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Criticals extends Check {
   public Criticals(CheckManager checkManager) {
      super(checkManager, HackType.CRITICALS, CheckPlayerData::new, null, true, (player, data) -> true);
   }

   private boolean canDealCriticalHit(Player player) {
      return player.getFallDistance() > 0.0F
         && !player.isOnGround()
         && !player.isInsideVehicle()
         && !player.hasPotionEffect(PotionEffectType.BLINDNESS)
         && !EnvironmentBlockChecks.isOnClimbable(new RelativeBlockAccess(player.getLocation()))
         && !EnvironmentBlockChecks.isInLiquid(new RelativeBlockAccess(player.getLocation()));
   }

   @Override
   public MovementResolution move(Player player, PlayerData playerData, MovementTransition movementTransition) {
      Vector vector = movementTransition.getFrom().toVector();
      Player player2 = player;
      BoundingBox box = new BoundingBox(vector, EntityDimensions.forEntity(player2));
      if (CollisionBoxAccess.anyBlockMatches(
         player.getWorld(), box, block -> block.getType().isBlock() || block.getType() == MaterialGroup.COBWEB.getMaterial() || block.isLiquid()
      )) {
         return null;
      }

      if (CollisionBoxAccess.anyBlockMatches(
         player.getWorld(), box.expand(1.0, 1.0, 1.0), block -> block.getType() == Material.SLIME_BLOCK
      )) {
         return null;
      }

      for (PotionEffect potionEffect : player.getActivePotionEffects()) {
         if (potionEffect.getType().equals(PotionEffectType.JUMP)) {
            return null;
         }
      }

      for (Entity entity : player.getNearbyEntities(2.0, 2.0, 2.0)) {
         if (entity instanceof Boat) {
            return null;
         }
      }

      String str = "check_minijump";
      if (this.getBooleanOption(str) && movementTransition.getFromGroundState().isOnGround()) {
         double y = movementTransition.getTo().getY() - movementTransition.getFrom().getY();
         if (y > 0.0 && y < 0.2) {
            double y2 = playerData.getMovementState().lastVelocity.getY();
            if (y2 > 0.0 && y2 < 0.2) {
               String str2 = "tried to do a mini-jump";
               byte b = 2;
               Player player3 = player;
               return this.addViolation(player3, b, str2) ? MovementResolution.setback() : null;
            }
         }
      }

      return null;
   }

   @Override
   public void attack(Player player, PlayerData playerData, EntityDamageByEntityEvent event) {
      Killaura.unusedFlag();
      Location location = player.getLocation();
      Player player2 = player;
      boolean flag = CollisionBoxAccess.isOnBlockSurface(player2, location);
      Player player3 = player;
      if (this.shouldMitigate(player3)) {
         event.setCancelled(true);
      } else {
         if (this.canDealCriticalHit(player) && flag) {
            String str = "tried to perform a critical hit whilst on the ground";
            byte b = 2;
            Player player4 = player;
            if (this.addViolation(player4, b, str)) {
               event.setCancelled(true);
            }
         }
      }
   }
}
