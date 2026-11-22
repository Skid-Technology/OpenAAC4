package me.konsolas.aac.movement;


import me.konsolas.aac.util.BoundingBox;
import java.util.List;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public interface EntityPhysicsContext {
   public EntityType getEntityType();

   public boolean hasSlowFalling();

   public float getBaseMovementSpeed();

   public boolean hasLevitation();

   public int getLevitationAmplifier();

   public boolean hasJumpBoost();

   public int getJumpBoostAmplifier();

   public boolean isInWater();

   public boolean isInLava();

   public double getWaterLevel();

   public int getDepthStriderLevel();

   public boolean hasDolphinsGrace();

   public float getBlockFriction();

   public double getJumpStrength();

   public double getStepHeight();

   public boolean isClimbable();

   public boolean isInCobweb();

   public boolean isInBerryBush();

   public boolean isTouchingHoney();

   public boolean isInBubbleColumn();

   public boolean isTorsoInLiquid();

   public boolean intersectsLiquidAtOffset(double d, double d2, double d3);

   public boolean isSlimeBelow(Vector vector);

   public boolean isBedBelow(Vector vector);

   public int getSoulSandContacts();

   public boolean isOnHoney();

   public BoundingBox getBoundingBox();

   public PlayerPose getPose();

   public List<BoundingBox> getIntersectingCollisionBoxes(BoundingBox box);

   public boolean isMovementCollisionFree(BoundingBox box, double d, double d2, double d3);
}
