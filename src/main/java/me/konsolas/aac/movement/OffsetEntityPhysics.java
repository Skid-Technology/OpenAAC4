package me.konsolas.aac.movement;


import me.konsolas.aac.util.BoundingBox;
import java.util.List;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class OffsetEntityPhysics implements EntityPhysicsContext {
   private final EntityPhysicsContext entityPhysicsContextValue;
   private final Vector offset;

   public OffsetEntityPhysics(EntityPhysicsContext entityPhysicsContext, Vector vector) {
      this.entityPhysicsContextValue = entityPhysicsContext;
      this.offset = vector;
   }

   @Override
   public EntityType getEntityType() {
      return this.entityPhysicsContextValue.getEntityType();
   }

   @Override
   public boolean hasSlowFalling() {
      return this.entityPhysicsContextValue.hasSlowFalling();
   }

   @Override
   public float getBaseMovementSpeed() {
      return this.entityPhysicsContextValue.getBaseMovementSpeed();
   }

   @Override
   public boolean hasLevitation() {
      return this.entityPhysicsContextValue.hasLevitation();
   }

   @Override
   public int getLevitationAmplifier() {
      return this.entityPhysicsContextValue.getLevitationAmplifier();
   }

   @Override
   public boolean hasJumpBoost() {
      return this.entityPhysicsContextValue.hasJumpBoost();
   }

   @Override
   public int getJumpBoostAmplifier() {
      return this.entityPhysicsContextValue.getJumpBoostAmplifier();
   }

   @Override
   public boolean isInWater() {
      return this.entityPhysicsContextValue.isInWater();
   }

   @Override
   public boolean isInLava() {
      return this.entityPhysicsContextValue.isInLava();
   }

   @Override
   public double getWaterLevel() {
      return this.entityPhysicsContextValue.getWaterLevel();
   }

   @Override
   public int getDepthStriderLevel() {
      return this.entityPhysicsContextValue.getDepthStriderLevel();
   }

   @Override
   public boolean hasDolphinsGrace() {
      return this.entityPhysicsContextValue.hasDolphinsGrace();
   }

   @Override
   public float getBlockFriction() {
      return this.entityPhysicsContextValue.getBlockFriction();
   }

   @Override
   public double getJumpStrength() {
      return this.entityPhysicsContextValue.getJumpStrength();
   }

   @Override
   public double getStepHeight() {
      return this.entityPhysicsContextValue.getStepHeight();
   }

   @Override
   public boolean isClimbable() {
      return this.entityPhysicsContextValue.isClimbable();
   }

   @Override
   public boolean isInCobweb() {
      return this.entityPhysicsContextValue.isInCobweb();
   }

   @Override
   public boolean isInBerryBush() {
      return this.entityPhysicsContextValue.isInBerryBush();
   }

   @Override
   public boolean isTouchingHoney() {
      return this.entityPhysicsContextValue.isTouchingHoney();
   }

   @Override
   public boolean isInBubbleColumn() {
      return this.entityPhysicsContextValue.isInBubbleColumn();
   }

   @Override
   public boolean isTorsoInLiquid() {
      return this.entityPhysicsContextValue.isTorsoInLiquid();
   }

   @Override
   public boolean intersectsLiquidAtOffset(double d, double d2, double d3) {
      return this.entityPhysicsContextValue.intersectsLiquidAtOffset(d, d2, d3);
   }

   @Override
   public boolean isSlimeBelow(Vector vector) {
      return this.entityPhysicsContextValue.isSlimeBelow(vector);
   }

   @Override
   public boolean isBedBelow(Vector vector) {
      return this.entityPhysicsContextValue.isBedBelow(vector);
   }

   @Override
   public int getSoulSandContacts() {
      return this.entityPhysicsContextValue.getSoulSandContacts();
   }

   @Override
   public boolean isOnHoney() {
      return this.entityPhysicsContextValue.isOnHoney();
   }

   @Override
   public BoundingBox getBoundingBox() {
      return this.entityPhysicsContextValue.getBoundingBox().translate(this.offset);
   }

   @Override
   public PlayerPose getPose() {
      return this.entityPhysicsContextValue.getPose();
   }

   @Override
   public List<BoundingBox> getIntersectingCollisionBoxes(BoundingBox box) {
      return this.entityPhysicsContextValue.getIntersectingCollisionBoxes(box);
   }

   @Override
   public boolean isMovementCollisionFree(BoundingBox box, double d, double d2, double d3) {
      return this.entityPhysicsContextValue.isMovementCollisionFree(box, d, d2, d3);
   }
}
