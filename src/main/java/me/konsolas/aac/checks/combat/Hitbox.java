package me.konsolas.aac.checks.combat;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.checks.movement.MovementTransition;
import me.konsolas.aac.compat.WorldRayTraceAccess;
import me.konsolas.aac.movement.EntityDimensions;
import me.konsolas.aac.movement.MovementResolution;
import me.konsolas.aac.util.BlockRayTraceResult;
import me.konsolas.aac.util.BoundingBox;
import com.google.common.collect.EvictingQueue;
import java.util.Optional;
import me.konsolas.aac.api.HackType;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class Hitbox extends Check {
   public Hitbox(CheckManager checkManager) {
      super(checkManager, HackType.HITBOX, HitboxPlayerData::new, null, true, (player, data) -> true);
   }

   @Override
   public MovementResolution move(Player player, PlayerData playerData, MovementTransition movementTransition) {
      HitboxPlayerData data = (HitboxPlayerData)playerData.getCheckData(HackType.HITBOX);
      if (HitboxPlayerData.getPositionHistory(data) == null) {
         String str = "buffer_size";
         HitboxPlayerData.setPositionHistory(data, EvictingQueue.create(this.getIntOption(str)));
      }

      HitboxPlayerData.getPositionHistory(data).add(movementTransition.getFrom().toVector());
      return null;
   }

   @Override
   public void attack(Player player, PlayerData playerData, EntityDamageByEntityEvent event) {
      if (event.getEntity().getVehicle() == null) {
         EvictingQueue<Vector> evictingQueue = null;
         if (event.getEntity() instanceof Player) {
            Player player2 = (Player)event.getEntity();
            Optional<HitboxPlayerData> optional = this.getPlayerData(player2).map(d -> (HitboxPlayerData)d.getCheckData(HackType.HITBOX));
            if (optional.isPresent()) {
               evictingQueue = HitboxPlayerData.getPositionHistory(optional.get());
            }
         }

         Vector vector = player.getEyeLocation().toVector();
         Vector vector2 = player.getLocation().getDirection();
         String str = "max_reach";
         double d2 = this.getDoubleOption(str);
         String str2 = "check_walls";
         double d3;
         Material material;
         if (this.getBooleanOption(str2)) {
            BlockRayTraceResult blockRayTraceResult = WorldRayTraceAccess.rayTraceBlock(player, vector, vector2, d2 + 1.5);
            d3 = blockRayTraceResult.isHit() ? blockRayTraceResult.getHitPosition().distance(vector) : Double.POSITIVE_INFINITY;
            material = blockRayTraceResult.isHit() ? blockRayTraceResult.getBlockPosition().toLocation(player.getWorld()).getBlock().getType() : null;
         } else {
            d3 = Double.POSITIVE_INFINITY;
            material = null;
         }

         if (evictingQueue != null && !evictingQueue.isEmpty()) {
            double d4;
            label94: {
               d4 = Double.POSITIVE_INFINITY;
               String str3 = "lenience";
               double d5 = this.getDoubleOption(str3);
               Vector vector3 = new Vector(d5, d5, d5);
               Entity entity = event.getEntity();
               EntityDimensions entityDimensions = EntityDimensions.forEntity(entity);
               BoundingBox box = new BoundingBox(event.getEntity().getLocation().toVector(), entityDimensions).expand(vector3);
               double d6 = box.rayTraceDistance(vector, vector2);
               if (!Double.isNaN(d6) && d6 < d4) {
                  d4 = d6;
                  if (d4 <= d2) {
                     break label94;
                  }
               }

               for (Vector vector4 : evictingQueue) {
                  BoundingBox box2 = new BoundingBox(vector4, entityDimensions).expand(vector3);
                  double d7 = box2.rayTraceDistance(vector, vector2);
                  if (!Double.isNaN(d7) && d7 < d4) {
                     d4 = d7;
                     if (d4 <= d2) {
                        break;
                     }
                  }
               }
            }

            if (!Double.isFinite(d4)) {
               String str4 = "missed player hitbox in attack";
               byte b = 2;
               Player player3 = player;
               if (this.addViolation(player3, b, str4)) {
                  event.setCancelled(true);
               }
            } else if (d4 > d2) {
               String str5 = String.format("attacked player too far away (%.3f blocks)", d4);
               byte b2 = 3;
               Player player4 = player;
               if (this.addViolation(player4, b2, str5)) {
                  event.setCancelled(true);
               }
            } else if (d4 > d3) {
               String str6 = "attacked player through " + material;
               byte b3 = 2;
               Player player5 = player;
               if (this.addViolation(player5, b3, str6)) {
                  event.setCancelled(true);
               }
            }
         } else {
            Vector vector5 = event.getEntity().getLocation().toVector();
            String str7 = "mob_lenience";
            double d8 = this.getDoubleOption(str7);
            Entity entity2 = event.getEntity();
            BoundingBox box3 = new BoundingBox(vector5, EntityDimensions.forEntity(entity2)).expand(d8, d8, d8);
            double d9 = box3.rayTraceDistance(vector, vector2);
            if (Double.isNaN(d9)) {
               String str8 = "missed entity hitbox in attack";
               byte b4 = 1;
               Player player6 = player;
               if (this.addViolation(player6, b4, str8)) {
                  event.setCancelled(true);
               }
            } else if (d9 > d2) {
               String str9 = String.format("attacked entity too far away (%.3f blocks)", d9);
               byte b5 = 1;
               Player player7 = player;
               if (this.addViolation(player7, b5, str9)) {
                  event.setCancelled(true);
               }
            } else if (d9 > d3) {
               String str10 = "attacked entity through " + material;
               byte b6 = 1;
               Player player8 = player;
               if (this.addViolation(player8, b6, str10)) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }
}
