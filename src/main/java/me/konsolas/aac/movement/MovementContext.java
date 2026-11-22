package me.konsolas.aac.movement;


import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.checks.movement.MovePlayerData;
import me.konsolas.aac.checks.movement.MovementTransition;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MovementContext {
   private final Player player;
   private final PlayerData playerData;
   private final MovementTransition transition;
   private final MovePlayerData checkData;
   private final Vector movement;
   private final Boolean elytraGliding;
   private final Boolean sneaking;
   private final Boolean usingItem;
   private final EntityPhysicsContext physics;
   private final WaterContactState waterContact;
   private final float pitch;
   private final float yaw;
   private final boolean hasKnockbackItem;
   private final boolean hasNearbyEntities;
   private final boolean canSprint;
   private final boolean nearShulkerBox;
   private final boolean nearBoat;
   private final double entityPushStrength;

   public MovementContext(
      Player player,
      PlayerData playerData,
      MovementTransition movementTransition,
      MovePlayerData data,
      Vector vector,
      Boolean boolValue,
      Boolean boolValue2,
      Boolean boolValue3,
      EntityPhysicsContext entityPhysicsContext,
      WaterContactState waterContactState,
      float f,
      float f2,
      boolean flag,
      boolean flag2,
      boolean flag3,
      boolean flag4,
      boolean flag5,
      double d
   ) {
      this.player = player;
      this.playerData = playerData;
      this.transition = movementTransition;
      this.checkData = data;
      this.movement = vector;
      this.elytraGliding = boolValue;
      this.sneaking = boolValue2;
      this.usingItem = boolValue3;
      this.physics = entityPhysicsContext;
      this.waterContact = waterContactState;
      this.pitch = f;
      this.yaw = f2;
      this.hasKnockbackItem = flag;
      this.hasNearbyEntities = flag2;
      this.canSprint = flag3;
      this.nearShulkerBox = flag4;
      this.nearBoat = flag5;
      this.entityPushStrength = d;
   }

   public Player getPlayer() {
      return this.player;
   }

   public PlayerData getPlayerData() {
      return this.playerData;
   }

   public MovementTransition getTransition() {
      return this.transition;
   }

   public MovePlayerData getCheckData() {
      return this.checkData;
   }

   public Vector getMovement() {
      return this.movement;
   }

   public Boolean getElytraGliding() {
      return this.elytraGliding;
   }

   public Boolean getSneaking() {
      return this.sneaking;
   }

   public Boolean getUsingItem() {
      return this.usingItem;
   }

   public EntityPhysicsContext getPhysics() {
      return this.physics;
   }

   public WaterContactState getWaterContact() {
      return this.waterContact;
   }

   public float getPitch() {
      return this.pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public boolean hasKnockbackItem() {
      return this.hasKnockbackItem;
   }

   public boolean hasNearbyEntities() {
      return this.hasNearbyEntities;
   }

   public boolean canSprint() {
      return this.canSprint;
   }

   public boolean isNearShulkerBox() {
      return this.nearShulkerBox;
   }

   public boolean isNearBoat() {
      return this.nearBoat;
   }

   public double getEntityPushStrength() {
      return this.entityPushStrength;
   }

}
