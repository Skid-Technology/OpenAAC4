package me.konsolas.aac.checks.movement;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.CheckPlayerData;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.compat.CollisionBoxAccess;
import me.konsolas.aac.compat.PlayerStateAccess;
import me.konsolas.aac.movement.EntityDimensions;
import me.konsolas.aac.movement.MovementResolution;
import me.konsolas.aac.util.BoundingBox;
import me.konsolas.aac.util.CollisionResult;
import me.konsolas.aac.util.CollisionUtils;
import me.konsolas.aac.util.VectorFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import me.konsolas.aac.api.HackType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Phase extends Check {
   private final Set<Material> excludedMaterials;

   public Phase(CheckManager checkManager) {
      super(checkManager, HackType.PHASE, CheckPlayerData::new, null, true, (player, data) -> true);
      this.excludedMaterials = Collections.unmodifiableSet(this.getStringListOption("exclude").stream().map(Material::getMaterial).collect(Collectors.toSet()));
   }

   @Override
   public MovementResolution move(Player player, PlayerData playerData, MovementTransition movementTransition) {
      if (player.getVehicle() == null) {
         Player player2 = player;
         if (!PlayerStateAccess.isGliding(player2)) {
            double d = movementTransition.getTo().distance(movementTransition.getFrom());
            if (d > 10.0) {
               Locale locale = Locale.UK;
               Object[] objArray = new Object[3];
               Vector vector = movementTransition.getFrom().toVector();
               objArray[0] = VectorFormatter.formatVector(vector);
               Vector vector2 = movementTransition.getTo().toVector();
               objArray[1] = VectorFormatter.formatVector(vector2);
               objArray[2] = d;
               String str = String.format(locale, "moved too far away from safe location %s -> %s (%.2f)", objArray);
               byte b = 1;
               Player player3 = player;
               if (this.addViolation(player3, b, str)) {
                  return MovementResolution.setback();
               }

               return null;
            }

            String str2 = "tolerance";
            double d2 = this.getDoubleOption(str2);
            Vector vector3 = movementTransition.getFrom().toVector();
            Player player4 = player;
            BoundingBox box2 = new BoundingBox(vector3, EntityDimensions.forEntity(player4)).expand(-d2, -d2, -d2);
            vector3 = movementTransition.getTo().toVector();
            Player player5 = player;
            BoundingBox box3 = new BoundingBox(vector3, EntityDimensions.forEntity(player5)).expand(-d2, -d2, -d2);
            if (box2.intersects(box3) && !movementTransition.getFromGroundState().hasCollided() && !movementTransition.getToGroundState().hasCollided()) {
               return null;
            }

            BoundingBox box4 = box2.union(box3);
            BoundingBox box5 = box4;
            Player player6 = player;
            List<BoundingBox> list = CollisionBoxAccess.getCubes(player, box4);
            if (CollisionBoxAccess.getCubes(player, box4).size() == 0) {
               return null;
            }

            List<BoundingBox> list2 = list.stream().filter(box -> box.intersects(box2)).collect(Collectors.toList());
            List<BoundingBox> list3 = list.stream().filter(box -> box.intersects(box3)).collect(Collectors.toList());
            if (!this.excludedMaterials.isEmpty()) {
               list2.removeIf(box -> this.excludedMaterials.contains(box.getMin().toLocation(player.getWorld()).getBlock().getType()));
               list3.removeIf(box -> this.excludedMaterials.contains(box.getMin().toLocation(player.getWorld()).getBlock().getType()));
            }

            if (list2.isEmpty() && !list3.isEmpty()) {
               BoundingBox box6 = (BoundingBox)list3.get(0);
               Block block = box6.getMin().toLocation(player.getWorld()).getBlock();
               String str3 = String.format(
                  Locale.UK,
                  "moved into a block (%s at %d,%d,%d)",
                  block.getType().toString(),
                  block.getLocation().getBlockX(),
                  block.getLocation().getBlockY(),
                  block.getLocation().getBlockZ()
               );
               byte b2 = 1;
               Player player7 = player;
               return this.addViolation(player7, b2, str3) ? MovementResolution.setback() : null;
            }

            if (!list2.isEmpty()) {
               if (d >= 1.0 || list3.size() >= list2.size() && !list2.containsAll(list3)) {
                  String str4 = String.format(
                     Locale.UK,
                     "moved inside blocks (%d,%d,%d to %d,%d,%d)",
                     movementTransition.getFrom().getBlockX(),
                     movementTransition.getFrom().getBlockY(),
                     movementTransition.getFrom().getBlockZ(),
                     movementTransition.getTo().getBlockX(),
                     movementTransition.getTo().getBlockY(),
                     movementTransition.getTo().getBlockZ()
                  );
                  byte b3 = 1;
                  Player player8 = player;
                  return this.addViolation(player8, b3, str4) ? MovementResolution.setback() : null;
               }

               return null;
            }

            if (box2.intersects(box3)) {
               if (!list2.isEmpty() || !list3.isEmpty()) {
                  String str5 = String.format(
                     Locale.UK,
                     "moved through a block (%d,%d,%d to %d,%d,%d)",
                     movementTransition.getFrom().getBlockX(),
                     movementTransition.getFrom().getBlockY(),
                     movementTransition.getFrom().getBlockZ(),
                     movementTransition.getTo().getBlockX(),
                     movementTransition.getTo().getBlockY(),
                     movementTransition.getTo().getBlockZ()
                  );
                  byte b4 = 1;
                  Player player9 = player;
                  return this.addViolation(player9, b4, str5) ? MovementResolution.setback() : null;
               }
            } else {
               Vector vector4 = movementTransition.getTo().clone().subtract(movementTransition.getFrom()).toVector();
               Vector vector5 = vector4.clone();
               Location location = movementTransition.getFrom();
               Vector vector6 = vector5;
               Location location2 = location;
               Player player10 = player;
               CollisionResult collisionResult = CollisionUtils.traceCollisions(player10, location2, vector6);
               if (!vector5.equals(vector4)) {
                  if (!(collisionResult.getDistance() > 0.0)) {
                     BoundingBox box7 = (BoundingBox)collisionResult.getCollisionBoxes().get(0);
                     Block block2 = box7.getMin().toLocation(player.getWorld()).getBlock();
                     String str6 = String.format(
                        Locale.UK,
                        "moved through a block (%s at %d,%d,%d)",
                        block2.getType().toString(),
                        block2.getLocation().getBlockX(),
                        block2.getLocation().getBlockY(),
                        block2.getLocation().getBlockZ()
                     );
                     byte b5 = 1;
                     Player player11 = player;
                     return this.addViolation(player11, b5, str6) ? MovementResolution.setback() : null;
                  }

                  Location location3 = movementTransition.getFrom().clone().add(0.0, collisionResult.getDistance() + 0.01, 0.0);
                  vector4 = movementTransition.getTo().clone().subtract(location3).toVector();
                  vector5 = vector4.clone();
                  Vector vector7 = vector5;
                  Location location4 = location3;
                  Player player12 = player;
                  collisionResult = CollisionUtils.traceCollisions(player12, location4, vector7);
                  if (!vector5.equals(vector4)) {
                     BoundingBox box8 = (BoundingBox)collisionResult.getCollisionBoxes().get(0);
                     Block block3 = box8.getMin().toLocation(player.getWorld()).getBlock();
                     String str7 = String.format(
                        Locale.UK,
                        "stepped through a block (%s at %d,%d,%d)",
                        block3.getType().toString(),
                        block3.getLocation().getBlockX(),
                        block3.getLocation().getBlockY(),
                        block3.getLocation().getBlockZ()
                     );
                     byte b6 = 1;
                     Player player13 = player;
                     return this.addViolation(player13, b6, str7) ? MovementResolution.setback() : null;
                  }
               }
            }

            return null;
         }
      }

      return null;
   }
}
