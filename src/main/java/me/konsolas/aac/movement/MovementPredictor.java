package me.konsolas.aac.movement;


import me.konsolas.aac.compat.ProtocolVersion;
import me.konsolas.aac.util.BoundingBox;
import me.konsolas.aac.util.FastMath;
import me.konsolas.aac.util.GroundCollisionState;
import me.konsolas.aac.util.VectorFormatter;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class MovementPredictor {
   private final EntityPhysicsContext context;
   private final Vector referenceVector;
   private final ProtocolVersion clientVersion;

   public MovementPredictor(EntityPhysicsContext entityPhysicsContext, Vector vector, ProtocolVersion protocolVersion) {
      this.context = entityPhysicsContext;
      this.referenceVector = vector.clone();
      this.clientVersion = protocolVersion;
   }

   public EntityPhysicsContext getContext() {
      return this.context;
   }

   public ProtocolVersion getClientVersion() {
      return this.clientVersion;
   }

   public MotionPrediction predict(MotionPrediction motionPrediction, MovementInput movementInput, VelocityState velocityState) {
      double y = this.clientVersion.isAtLeast(ProtocolVersion.V1_13)
            && this.context.hasSlowFalling()
            && motionPrediction.getVectorState().getVelocity().getY() <= 0.0
         ? 0.01
         : 0.08;
      MovementControls movementControls = this.computeControls(motionPrediction.getVectorState().isOnGround(), movementInput);
      MotionVectorState motionVectorState = this.integrateVelocity(motionPrediction, movementInput, movementControls, velocityState, y);
      return new MotionPrediction(movementInput, velocityState, movementControls, motionVectorState);
   }

   public MotionPrediction createInitialPrediction(Location location, GroundCollisionState groundCollisionState, GroundCollisionState groundCollisionState2, boolean flag, Vector vector) {
      MotionPrediction.unusedPredictionFlag();
      Vector vector2 = vector;
      if (groundCollisionState.isOnGround() && groundCollisionState2.isOnGround()) {
         vector2 = vector.clone().setY(0);
      }

      MovementControls pitch = this.computeControls(
         groundCollisionState.isOnGround(), new MovementInput(1.0F, 0.0F, flag, false, false, false, false, location.getPitch(), location.getYaw(), 0)
      );
      MotionVectorState motionVectorState = new MotionVectorState(vector, vector2, groundCollisionState2.isOnGround(), groundCollisionState2.hasCollided());
      return new MotionPrediction(MovementInput.EMPTY, null, pitch, motionVectorState);
   }

   private double getJumpVelocity() {
      MotionPrediction.unusedPredictionFlag();
      double d = this.context.getJumpStrength();
      if (this.context.isOnHoney()) {
         d *= 0.5;
      }

      if (this.context.hasJumpBoost()) {
         d += (this.context.getJumpBoostAmplifier() + 1) * 0.1;
      }

      return d;
   }

   private static Vector getLookVector(ProtocolVersion protocolVersion, float f, float f2) {
      MotionPrediction.unusedPredictionFlag();
      if (protocolVersion.isAtLeast(ProtocolVersion.V1_13)) {
         float f3 = f * (float) (Math.PI / 180.0);
         float f4 = -f2 * (float) (Math.PI / 180.0);
         float f5 = FastMath.cos(f4);
         float f6 = FastMath.sin(f4);
         float f7 = FastMath.cos(f3);
         float f8 = FastMath.sin(f3);
         return new Vector(f6 * f7, -f8, f5 * f7);
      } else {
         float f9 = FastMath.cos(-f2 * (float) (Math.PI / 180.0) - (float) Math.PI);
         float f10 = FastMath.sin(-f2 * (float) (Math.PI / 180.0) - (float) Math.PI);
         float f11 = -FastMath.cos(-f * (float) (Math.PI / 180.0));
         float f12 = FastMath.sin(-f * (float) (Math.PI / 180.0));
         return new Vector(f10 * f11, f12, f9 * f11);
      }
   }

   private MotionVectorState integrateVelocity(MotionPrediction motionPrediction, MovementInput movementInput, MovementControls movementControls, VelocityState velocityState, double d) {
      Vector vector;
      if (velocityState != null && !velocityState.isApplied()) {
         vector = velocityState.getVelocity();
      } else {
         vector = motionPrediction.getVectorState().getVelocity().clone();
         if (this.context.isOnHoney()) {
            vector.setX(vector.getX() * 0.4);
            vector.setZ(vector.getZ() * 0.4);
         }

         if (this.context.getSoulSandContacts() > 0) {
            double d2 = Math.pow(0.4, this.context.getSoulSandContacts());
            vector.setX(vector.getX() * d2);
            vector.setZ(vector.getZ() * d2);
         }

         if (this.context.isTouchingHoney() && !motionPrediction.getVectorState().isOnGround() && motionPrediction.getVectorState().getVelocity().getY() < -0.08) {
            if (motionPrediction.getVectorState().getVelocity().getY() < -0.13) {
               double y = -0.05 / motionPrediction.getVectorState().getVelocity().getY();
               vector.setX(vector.getX() * y);
               vector.setZ(vector.getZ() * y);
            }

            vector.setY(-0.05);
         }

         if (motionPrediction.getControls().isInWater()) {
            vector.setX(vector.getX() * motionPrediction.getControls().getFriction());
            vector.setY(vector.getY() * 0.8);
            vector.setZ(vector.getZ() * motionPrediction.getControls().getFriction());
            if (this.clientVersion.isAtLeast(ProtocolVersion.V1_13)) {
               if (!movementInput.isSprinting()) {
                  if (vector.getY() <= 0.0 && Math.abs(vector.getY() - 0.005) >= 0.003 && Math.abs(vector.getY() - d / 16.0) < 0.003) {
                     vector.setY(-0.003);
                  } else {
                     vector.setY(vector.getY() - d / 16.0);
                  }
               }
            } else {
               vector.setY(vector.getY() - 0.02);
            }

            if (motionPrediction.getVectorState().hasHorizontalCollision()
               && !this.context.intersectsLiquidAtOffset(vector.getX(), vector.getY() + 0.6 - motionPrediction.getVectorState().getVelocity().getY(), vector.getZ())) {
               vector.setY(0.3F);
            }
         } else if (motionPrediction.getControls().isInLava()) {
            vector.multiply(0.5);
            if (this.clientVersion.isAtLeast(ProtocolVersion.V1_13)) {
               vector.setY(vector.getY() - d / 4.0);
            } else {
               vector.setY(vector.getY() - 0.02);
            }

            if (motionPrediction.getVectorState().hasHorizontalCollision()
               && !this.context.intersectsLiquidAtOffset(vector.getX(), vector.getY() + 0.6 - motionPrediction.getVectorState().getVelocity().getY(), vector.getZ())) {
               vector.setY(0.3F);
            }
         } else if (!motionPrediction.getControls().isElytra()) {
            if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_14)) {
               if ((motionPrediction.getVectorState().hasHorizontalCollision() || movementInput.isJumping()) && this.context.isClimbable()) {
                  vector.setY(0.2);
               }
            } else if (motionPrediction.getVectorState().hasHorizontalCollision() && this.context.isClimbable()) {
               vector.setY(0.2);
            }

            if (this.clientVersion.isAtLeast(ProtocolVersion.V1_9) && this.context.hasLevitation()) {
               vector.setY(vector.getY() + (0.05 * (this.context.getLevitationAmplifier() + 1) - vector.getY()) * 0.2);
            } else {
               vector.setY(vector.getY() - d);
            }

            vector.setY(vector.getY() * 0.98F);
            vector.setX(vector.getX() * motionPrediction.getControls().getFriction());
            vector.setZ(vector.getZ() * motionPrediction.getControls().getFriction());
         }

         if (velocityState != null && velocityState.isApplied()) {
            vector.add(velocityState.getVelocity());
         }
      }

      if (this.context.getEntityType() != EntityType.PLAYER) {
         vector.multiply(0.98);
      }

      for (int value = 0; value < movementInput.getAttackCount(); value++) {
         vector.setX(vector.getX() * 0.6);
         vector.setZ(vector.getZ() * 0.6);
      }

      if (this.clientVersion.isAtLeast(ProtocolVersion.V1_13) && movementControls.isInWater() && movementInput.isSneaking()) {
         vector.setY(vector.getY() - 0.04F);
      }

      if (this.clientVersion.isAtLeast(ProtocolVersion.V1_9)) {
         if (Math.abs(vector.getX()) < 0.003) {
            vector.setX(0);
         }

         if (Math.abs(vector.getY()) < 0.003) {
            vector.setY(0);
         }

         if (Math.abs(vector.getZ()) < 0.003) {
            vector.setZ(0);
         }
      } else {
         if (Math.abs(vector.getX()) < 0.005) {
            vector.setX(0);
         }

         if (Math.abs(vector.getY()) < 0.005) {
            vector.setY(0);
         }

         if (Math.abs(vector.getZ()) < 0.005) {
            vector.setZ(0);
         }
      }

      float f = movementInput.getForward() * 0.98F;
      float f2 = movementInput.getStrafe() * 0.98F;
      if (movementInput.isUsingItem()) {
         f *= 0.2F;
         f2 *= 0.2F;
      }

      boolean flag = movementInput.isSneaking();
      if (this.context.isInWater()) {
         if (this.clientVersion.isAtLeast(ProtocolVersion.V1_15) && movementInput.isSprinting()) {
            flag = false;
         }
      } else if (this.clientVersion.isAtLeast(ProtocolVersion.V1_13) && this.context.getPose() == PlayerPose.SWIMMING) {
         flag = true;
      }

      if (flag) {
         f = (float)(f * 0.3);
         f2 = (float)(f2 * 0.3);
      }

      if (movementInput.isJumping()) {
         if (this.clientVersion.isAtLeast(ProtocolVersion.V1_13)) {
            double d3 = this.context.getWaterLevel();
            if (!(d3 > 0.0) || motionPrediction.getVectorState().isOnGround() && d3 <= 0.4) {
               if (movementControls.isInLava()) {
                  vector.setY(vector.getY() + 0.04F);
               } else if (motionPrediction.getVectorState().isOnGround() || d3 > 0.0 && d3 <= 0.4) {
                  this.applyJumpImpulse(movementInput, vector);
               }
            } else {
               vector.setY(vector.getY() + 0.04F);
            }
         } else if (!movementControls.isInWater() && !movementControls.isInLava()) {
            this.applyJumpImpulse(movementInput, vector);
         } else {
            vector.setY(vector.getY() + 0.04F);
         }
      }

      if (this.clientVersion.isAtLeast(ProtocolVersion.V1_13) && movementControls.isInWater() && movementInput.isSprinting()) {
         double pitch = -FastMath.sin((float)Math.toRadians(movementInput.getPitch()));
         double d4 = pitch < -0.2 ? 0.085 : 0.06;
         if (pitch <= 0.0 || movementInput.isJumping() || this.context.isTorsoInLiquid()) {
            vector.setY(vector.getY() + (pitch - vector.getY()) * d4);
         }
      }

      if (!movementInput.isElytra() || movementControls.isInWater() || movementControls.isInLava()) {
         vector.add(computeMoveFlyingVector(f2, f, movementControls.getAcceleration(), movementInput.getYaw()));
      } else if (this.clientVersion.isAtLeast(ProtocolVersion.V1_13)) {
         Vector pitch2 = getLookVector(this.clientVersion, movementInput.getPitch(), movementInput.getYaw());
         float pitch3 = movementInput.getPitch() * (float) (Math.PI / 180.0);
         double x = FastMath.sqrt(pitch2.getX() * pitch2.getX() + pitch2.getZ() * pitch2.getZ());
         double x2 = FastMath.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
         double d5 = pitch2.length();
         float f3 = FastMath.cos(pitch3);
         f3 = (float)((double)f3 * f3 * Math.min(1.0, d5 / 0.4));
         vector.setY(vector.getY() + d * (-1.0 + f3 * 0.75));
         if (vector.getY() < 0.0 && x > 0.0) {
            double y2 = vector.getY() * -0.1 * f3;
            vector.setY(vector.getY() + y2);
            vector.setX(vector.getX() + pitch2.getX() * y2 / x);
            vector.setZ(vector.getZ() + pitch2.getZ() * y2 / x);
         }

         if (pitch3 < 0.0F && x > 0.0) {
            double d6 = x2 * -FastMath.sin(pitch3) * 0.04;
            vector.setY(vector.getY() + d6 * 3.2);
            vector.setX(vector.getX() - pitch2.getX() * d6 / x);
            vector.setZ(vector.getZ() - pitch2.getZ() * d6 / x);
         }

         if (x > 0.0) {
            vector.setX(vector.getX() + (pitch2.getX() / x * x2 - vector.getX()) * 0.1);
            vector.setZ(vector.getZ() + (pitch2.getZ() / x * x2 - vector.getZ()) * 0.1);
         }

         vector.setY(vector.getY() * 0.98);
         vector.setX(vector.getX() * 0.99);
         vector.setZ(vector.getZ() * 0.99);
      } else {
         Vector pitch4 = getLookVector(this.clientVersion, movementInput.getPitch(), movementInput.getYaw());
         float pitch5 = movementInput.getPitch() * (float) (Math.PI / 180.0);
         double x3 = FastMath.sqrt(pitch4.getX() * pitch4.getX() + pitch4.getZ() * pitch4.getZ());
         double x4 = FastMath.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
         double d7 = pitch4.length();
         float f4 = FastMath.cos(pitch5);
         f4 = (float)((double)f4 * f4 * Math.min(1.0, d7 / 0.4));
         vector.setY(vector.getY() - 0.08 + f4 * 0.06);
         if (vector.getY() < 0.0 && x3 > 0.0) {
            double y3 = vector.getY() * -0.1 * f4;
            vector.setY(vector.getY() + y3);
            vector.setX(vector.getX() + pitch4.getX() * y3 / x3);
            vector.setZ(vector.getZ() + pitch4.getZ() * y3 / x3);
         }

         if (pitch5 < 0.0F) {
            double d8 = x4 * -FastMath.sin(pitch5) * 0.04;
            vector.setY(vector.getY() + d8 * 3.2);
            vector.setX(vector.getX() - pitch4.getX() * d8 / x3);
            vector.setZ(vector.getZ() - pitch4.getZ() * d8 / x3);
         }

         if (x3 > 0.0) {
            vector.setX(vector.getX() + (pitch4.getX() / x3 * x4 - vector.getX()) * 0.1);
            vector.setZ(vector.getZ() + (pitch4.getZ() / x3 * x4 - vector.getZ()) * 0.1);
         }

         vector.setY(vector.getY() * 0.98);
         vector.setX(vector.getX() * 0.99);
         vector.setZ(vector.getZ() * 0.99);
      }

      if (!movementControls.isInWater() && !movementControls.isInLava() && !movementInput.isElytra() && this.context.isClimbable()) {
         vector.setX(VectorFormatter.clamp(vector.getX(), -0.15F, 0.15F));
         vector.setZ(VectorFormatter.clamp(vector.getZ(), -0.15F, 0.15F));
         if (vector.getY() < -0.15) {
            vector.setY(-0.15);
         }

         if (movementInput.isSneaking() && vector.getY() < 0.0) {
            vector.setY(0.0);
         }
      }

      return this.stepAndCollide(this.referenceVector, vector, motionPrediction.getVectorState().isOnGround(), movementInput.isSneaking());
   }

   private void applyJumpImpulse(MovementInput movementInput, Vector vector) {
      float f = this.context.getEntityType() == EntityType.PLAYER ? 0.2F : 0.4F;
      float yaw = movementInput.getYaw() * (float) (Math.PI / 180.0);
      Vector vector2 = movementInput.isSprinting() ? new Vector(-FastMath.sin(yaw) * f, 0.0F, FastMath.cos(yaw) * f) : new Vector(0, 0, 0);
      double d = this.getJumpVelocity();
      vector.add(vector2);
      vector.setY(d);
   }

   private MovementControls computeControls(boolean flag, MovementInput movementInput) {
      float f = this.context.getBaseMovementSpeed();
      if (movementInput.isSprinting()) {
         f = (float)(f * 1.300000011920929);
      }

      float f2;
      float f3;
      if (this.context.isInWater()) {
         f2 = this.clientVersion.isAtLeast(ProtocolVersion.V1_13) && movementInput.isSprinting() ? 0.9F : 0.8F;
         f3 = 0.02F;
         float f4 = this.context.getDepthStriderLevel();
         if (f4 > 3.0F) {
            f4 = 3.0F;
         }

         if (!flag) {
            f4 *= 0.5F;
         }

         if (f4 > 0.0F) {
            f2 = (float)(f2 + (0.546 - f2) * f4 / 3.0);
            f3 += (f * 1.0F - f3) * f4 / 3.0F;
         }

         if (this.clientVersion.isAtLeast(ProtocolVersion.V1_13) && this.context.hasDolphinsGrace()) {
            f2 = 0.96F;
         }
      } else if (this.context.isInLava()) {
         f3 = 0.02F;
         f2 = 0.5F;
      } else {
         f2 = 0.91F;
         if (flag) {
            f2 = this.context.getBlockFriction() * 0.91F;
         }

         float f5 = 0.16277136F / (f2 * f2 * f2);
         if (flag) {
            f3 = f * f5;
         } else if (this.context.getEntityType() == EntityType.PLAYER) {
            f3 = 0.02F;
            if (movementInput.isSprinting()) {
               f3 = 0.026F;
            }
         } else {
            f3 = f * 0.1F;
         }
      }

      return new MovementControls(
         this.clientVersion, f3, f2, this.context.isInWater(), this.context.isInLava(), movementInput.isElytra(), movementInput.isSprinting() && movementInput.getForward() < 0.8
      );
   }

   private static Vector computeMoveFlyingVector(float f, float f2, float f3, float f4) {
      MotionPrediction.unusedPredictionFlag();
      float f5 = f * f + f2 * f2;
      if (f5 >= 1.0E-4F) {
         f5 = FastMath.sqrt(f5);
         if (f5 < 1.0F) {
            f5 = 1.0F;
         }

         f5 = f3 / f5;
         f *= f5;
         f2 *= f5;
         float f6 = FastMath.sin(f4 * (float) Math.PI / 180.0F);
         float f7 = FastMath.cos(f4 * (float) Math.PI / 180.0F);
         return new Vector(f * f7 - f2 * f6, 0.0F, f2 * f7 + f * f6);
      } else {
         return new Vector(0, 0, 0);
      }
   }

   private MotionVectorState stepAndCollide(Vector vector, Vector vector2, boolean flag, boolean flag2) {
      Vector vector3 = vector2.clone();
      if (this.context.isInCobweb()) {
         vector3.setX(vector2.getX() * 0.25);
         vector3.setY(vector2.getY() * 0.05F);
         vector3.setZ(vector2.getZ() * 0.25);
      } else if (this.context.isInBerryBush()) {
         vector3.setX(vector2.getX() * 0.8F);
         vector3.setY(vector2.getY() * 0.75);
         vector3.setZ(vector2.getZ() * 0.8F);
      }

      Vector vector4 = vector3.clone();
      BoundingBox box = this.context.getBoundingBox();
      BoundingBox box2 = box;
      List<BoundingBox> list = this.context.getIntersectingCollisionBoxes(box.expandTowards(vector2));
      if (flag && flag2) {
         double d;
         for (d = this.clientVersion.isAtLeast(ProtocolVersion.V1_9) ? -this.context.getStepHeight() : -1.0;
            vector3.getX() != 0.0 && this.context.isMovementCollisionFree(this.context.getBoundingBox(), vector3.getX(), d, 0.0);
            vector4.setX(vector3.getX())
         ) {
            if (vector3.getX() < 0.05 && vector3.getX() >= -0.05) {
               vector3.setX(0.0);
            } else if (vector3.getX() > 0.0) {
               vector3.setX(vector3.getX() - 0.05);
            } else {
               vector3.setX(vector3.getX() + 0.05);
            }
         }

         for (; vector3.getZ() != 0.0 && this.context.isMovementCollisionFree(this.context.getBoundingBox(), 0.0, d, vector3.getZ()); vector4.setZ(vector3.getZ())) {
            if (vector3.getZ() < 0.05 && vector3.getZ() >= -0.05) {
               vector3.setZ(0.0);
            } else if (vector3.getZ() > 0.0) {
               vector3.setZ(vector3.getZ() - 0.05);
            } else {
               vector3.setZ(vector3.getZ() + 0.05);
            }
         }

         for (;
            vector3.getX() != 0.0 && vector3.getZ() != 0.0 && this.context.isMovementCollisionFree(this.context.getBoundingBox(), vector3.getX(), d, vector3.getZ());
            vector4.setZ(vector3.getZ())
         ) {
            if (vector3.getX() < 0.05 && vector3.getX() >= -0.05) {
               vector3.setX(0.0);
            } else if (vector3.getX() > 0.0) {
               vector3.setX(vector3.getX() - 0.05);
            } else {
               vector3.setX(vector3.getX() + 0.05);
            }

            vector4.setX(vector3.getX());
            if (vector3.getZ() < 0.05 && vector3.getZ() >= -0.05) {
               vector3.setZ(0.0);
            } else if (vector3.getZ() > 0.0) {
               vector3.setZ(vector3.getZ() - 0.05);
            } else {
               vector3.setZ(vector3.getZ() + 0.05);
            }
         }
      }

      if (this.clientVersion.isAtLeast(ProtocolVersion.V1_14)) {
         box = box.expand(-1.0E-7, -1.0E-7, -1.0E-7);
      }

      for (BoundingBox box3 : list) {
         vector3.setY(box3.calculateYOffset(box, vector3.getY()));
      }

      box = box.translate(0.0, vector3.getY(), 0.0);
      boolean y = flag || vector3.getY() != vector4.getY() && vector4.getY() < 0.0;
      if (this.clientVersion.isAtLeast(ProtocolVersion.V1_13)) {
         boolean x = Math.abs(vector3.getX()) < Math.abs(vector3.getZ());
         if (x && vector3.getZ() != 0.0) {
            for (BoundingBox box4 : list) {
               vector3.setZ(box4.calculateZOffset(box, vector3.getZ()));
            }

            box = box.translate(0.0, 0.0, vector3.getZ());
         }

         if (vector3.getX() != 0.0) {
            for (BoundingBox box5 : list) {
               vector3.setX(box5.calculateXOffset(box, vector3.getX()));
            }

            box = box.translate(vector3.getX(), 0.0, 0.0);
         }

         if (!x && vector3.getZ() != 0.0) {
            for (BoundingBox box6 : list) {
               vector3.setZ(box6.calculateZOffset(box, vector3.getZ()));
            }

            box = box.translate(0.0, 0.0, vector3.getZ());
         }
      } else {
         for (BoundingBox box7 : list) {
            vector3.setX(box7.calculateXOffset(box, vector3.getX()));
         }

         box = box.translate(vector3.getX(), 0.0, 0.0);

         for (BoundingBox box8 : list) {
            vector3.setZ(box8.calculateZOffset(box, vector3.getZ()));
         }

         box = box.translate(0.0, 0.0, vector3.getZ());
      }

      if (y && (vector3.getX() != vector4.getX() || vector3.getZ() != vector4.getZ())) {
         double x2 = vector3.getX();
         double y2 = vector3.getY();
         double z = vector3.getZ();
         BoundingBox box9 = box;
         box = box2;
         vector3.setY(this.context.getStepHeight());
         List<BoundingBox> x3 = this.context.getIntersectingCollisionBoxes(box9.expandTowards(new Vector(vector4.getX(), vector3.getY(), vector4.getZ())));
         BoundingBox box10 = box;
         BoundingBox x4 = box10.expandTowards(new Vector(vector4.getX(), 0.0, vector4.getZ()));
         double y3 = vector3.getY();

         for (BoundingBox box11 : x3) {
            y3 = box11.calculateYOffset(x4, y3);
         }

         box10 = box10.translate(0.0, y3, 0.0);
         double x5 = vector4.getX();

         for (BoundingBox box12 : x3) {
            x5 = box12.calculateXOffset(box10, x5);
         }

         box10 = box10.translate(x5, 0.0, 0.0);
         double z2 = vector4.getZ();

         for (BoundingBox box13 : x3) {
            z2 = box13.calculateZOffset(box10, z2);
         }

         box10 = box10.translate(0.0, 0.0, z2);
         BoundingBox box14 = box;
         double y4 = vector3.getY();

         for (BoundingBox box15 : x3) {
            y4 = box15.calculateYOffset(box14, y4);
         }

         box14 = box14.translate(0.0, y4, 0.0);
         double x6 = vector4.getX();

         for (BoundingBox box16 : x3) {
            x6 = box16.calculateXOffset(box14, x6);
         }

         box14 = box14.translate(x6, 0.0, 0.0);
         double z3 = vector4.getZ();

         for (BoundingBox box17 : x3) {
            z3 = box17.calculateZOffset(box14, z3);
         }

         box14 = box14.translate(0.0, 0.0, z3);
         double d2 = x5 * x5 + z2 * z2;
         double d3 = x6 * x6 + z3 * z3;
         if (d2 > d3) {
            vector3.setX(x5);
            vector3.setZ(z2);
            vector3.setY(-y3);
            box = box10;
         } else {
            vector3.setX(x6);
            vector3.setZ(z3);
            vector3.setY(-y4);
            box = box14;
         }

         for (BoundingBox box18 : x3) {
            vector3.setY(box18.calculateYOffset(box, vector3.getY()));
         }

         box = box.translate(0.0, vector3.getY(), 0.0);
         if (x2 * x2 + z * z >= vector3.getX() * vector3.getX() + vector3.getZ() * vector3.getZ()) {
            vector3.setX(x2);
            vector3.setY(y2);
            vector3.setZ(z);
            box = box9;
         }
      }

      boolean x7 = vector4.getX() != vector3.getX() || vector4.getZ() != vector3.getZ();
      boolean y5 = vector4.getY() != vector3.getY();
      boolean y6 = y5 && vector4.getY() < 0.0;
      if (!this.context.isInCobweb() && !this.context.isInBerryBush()) {
         if (y5 && this.context.isSlimeBelow(vector.clone().add(vector2)) && !flag2 && vector2.getY() < 0.0) {
            vector2.setY(-vector2.getY());
            if (Math.abs(vector2.getY()) < 0.1) {
               double y7 = 0.4 + Math.abs(vector2.getY()) * 0.2;
               vector2.setX(y7 * vector2.getX());
               vector2.setZ(y7 * vector2.getZ());
            }
         } else if (y5
            && this.context.isBedBelow(vector.clone().add(vector2))
            && !flag2
            && vector2.getY() < 0.0
            && this.clientVersion.isAtLeast(ProtocolVersion.V1_12)) {
            vector2.setY(-vector2.getY() * 0.66F);
         } else {
            if (vector4.getY() != vector3.getY()) {
               vector2.setY(0);
            }

            if (this.clientVersion.isAtLeast(ProtocolVersion.V1_13)) {
               if (vector4.getZ() != vector3.getZ()) {
                  vector2.setZ(0);
               } else if (vector4.getX() != vector3.getX()) {
                  vector2.setX(0);
               }
            } else {
               if (vector4.getX() != vector3.getX()) {
                  vector2.setX(0);
               }

               if (vector4.getZ() != vector3.getZ()) {
                  vector2.setZ(0);
               }
            }
         }
      } else {
         vector2 = new Vector(0, 0, 0);
      }

      return new MotionVectorState(box.getBottomCenter().subtract(vector), vector2, y6, x7);
   }
}
