package me.konsolas.aac.checks.movement;


import me.konsolas.aac.util.GroundCollisionState;
import me.konsolas.aac.util.RelativeBlockAccess;
import org.bukkit.Location;

public class MovementTransition {
   private final Location from;
   private final Location to;
   private final RelativeBlockAccess fromBlockAccess;
   private final RelativeBlockAccess toBlockAccess;
   private final GroundCollisionState fromGroundState;
   private final GroundCollisionState toGroundState;

   public MovementTransition(
      Location location, Location location2, RelativeBlockAccess relativeBlockAccess, RelativeBlockAccess relativeBlockAccess2, GroundCollisionState groundCollisionState, GroundCollisionState groundCollisionState2
   ) {
      this.from = location;
      this.to = location2;
      this.fromBlockAccess = relativeBlockAccess;
      this.toBlockAccess = relativeBlockAccess2;
      this.fromGroundState = groundCollisionState;
      this.toGroundState = groundCollisionState2;
   }

   public Location getFrom() {
      return this.from;
   }

   public Location getTo() {
      return this.to;
   }

   public RelativeBlockAccess getFromBlockAccess() {
      return this.fromBlockAccess;
   }

   public GroundCollisionState getFromGroundState() {
      return this.fromGroundState;
   }

   public GroundCollisionState getToGroundState() {
      return this.toGroundState;
   }
}
