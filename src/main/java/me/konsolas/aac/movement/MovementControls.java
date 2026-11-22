package me.konsolas.aac.movement;


import me.konsolas.aac.compat.ProtocolVersion;
import java.util.Objects;

public class MovementControls {
   private final ProtocolVersion clientVersion;
   private final float acceleration;
   private final float friction;
   private final boolean inWater;
   private final boolean inLava;
   private final boolean elytra;
   private final boolean slowedBySprint;

   public MovementControls(ProtocolVersion protocolVersion, float f, float f2, boolean flag, boolean flag2, boolean flag3, boolean flag4) {
      this.clientVersion = protocolVersion;
      this.acceleration = f;
      this.friction = f2;
      this.inWater = flag;
      this.inLava = flag2;
      this.elytra = flag3;
      this.slowedBySprint = flag4;
   }

   public float getAcceleration() {
      return this.acceleration;
   }

   public float getFriction() {
      return this.friction;
   }

   public boolean isInWater() {
      return this.inWater;
   }

   public boolean isInLava() {
      return this.inLava;
   }

   public boolean isElytra() {
      return this.elytra;
   }

   public boolean isSlowedBySprint() {
      return this.slowedBySprint;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         MovementControls movementControls = (MovementControls)obj;
         return Float.compare(movementControls.acceleration, this.acceleration) == 0
            && Float.compare(movementControls.friction, this.friction) == 0
            && this.inWater == movementControls.inWater
            && this.elytra == movementControls.elytra
            && this.clientVersion.equals(movementControls.clientVersion);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.clientVersion, this.acceleration, this.friction, this.inWater, this.elytra);
   }
}
