package me.konsolas.aac.movement;


import me.konsolas.aac.compat.CollisionBoxAccess;
import me.konsolas.aac.compat.PlayerStateAccess;
import me.konsolas.aac.compat.ProtocolVersion;
import me.konsolas.aac.util.BoundingBox;
import me.konsolas.aac.util.CollisionResolver;
import me.konsolas.aac.util.MaterialGroup;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class EntityPhysicsSnapshot implements EntityPhysicsContext {
   private final EntityType entityType;
   private final float baseMovementSpeed;
   private final double jumpStrength;
   private final double stepHeight;
   private final int levitationAmplifier;
   private final int jumpBoostAmplifier;
   private final boolean slowFalling;
   private final boolean dolphinsGrace;
   private final int depthStriderLevel;
   private final float blockFriction;
   private final BoundingBox boundingBox;
   private final PlayerPose pose;
   private final boolean climbable;
   private final boolean inCobweb;
   private final boolean inBerryBush;
   private final boolean touchingHoney;
   private final boolean inBubbleColumn;
   private final boolean torsoInLiquid;
   private final WaterContactState waterContact;
   private final boolean inLava;
   private final List<BoundingBox> collisionBoxes;
   private final BlockVector slimeBlockPosition;
   private final BlockVector bedBlockPosition;
   private final int soulSandContacts;
   private final boolean onHoney;
   private final Set sampledLiquidBlocks = new HashSet();

   public EntityPhysicsSnapshot(EntityPhysicsContext entityPhysicsContext, LivingEntity livingEntity, Location location, boolean flag, boolean flag2, ProtocolVersion protocolVersion, Vector vector) {
      this.entityType = livingEntity.getType();
      MotionPrediction.unusedPredictionFlag();
      int value = -1;
      int value2 = -1;
      int value3 = -1;
      int value4 = -1;
      boolean flag3 = false;
      boolean flag4 = false;

      for (PotionEffect potionEffect : livingEntity.getActivePotionEffects()) {
         if (potionEffect.getType().equals(PotionEffectType.SPEED)) {
            value = potionEffect.getAmplifier();
         } else if (potionEffect.getType().equals(PotionEffectType.SLOW)) {
            value2 = potionEffect.getAmplifier();
         } else if (potionEffect.getType().equals(PotionEffectType.JUMP)) {
            value3 = potionEffect.getAmplifier();
         } else if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_9) && potionEffect.getType().equals(PotionEffectType.LEVITATION)) {
            value4 = potionEffect.getAmplifier();
         } else if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13) && potionEffect.getType().equals(PotionEffectType.SLOW_FALLING)) {
            flag3 = true;
         } else if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13) && potionEffect.getType().equals(PotionEffectType.DOLPHINS_GRACE)) {
            flag4 = true;
         }
      }

      this.levitationAmplifier = value4;
      this.jumpBoostAmplifier = value3;
      this.slowFalling = flag3;
      this.dolphinsGrace = flag4;
      if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_9)) {
         float f = (float)Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).getValue();
         if (livingEntity instanceof Player && ((Player)livingEntity).isSprinting()) {
            f = (float)(f / 1.300000011920929);
         }

         this.baseMovementSpeed = f;
      } else {
         float f2;
         switch (EntityTypeSwitch.ordinalSwitch[this.entityType.ordinal()]) {
            case 1:
               f2 = ((Player)livingEntity).getWalkSpeed() / 2.0F;
               break;
            case 2:
               f2 = 0.25F;
               break;
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
               f2 = 0.3375F;
               break;
            default:
               throw new IllegalArgumentException("Player riding " + livingEntity.getType());
         }

         if (value >= 0) {
            f2 = (float)(f2 * (1.0 + 0.2 * (value + 1)));
         }

         if (value2 >= 0) {
            f2 = (float)(f2 * (1.0 - 0.15 * (value2 + 1)));
         }

         this.baseMovementSpeed = f2;
      }

      switch (EntityTypeSwitch.ordinalSwitch[this.entityType.ordinal()]) {
         case 1:
         case 2:
            this.jumpStrength = 0.42F;
            break;
         case 3:
            this.jumpStrength = ((Horse)livingEntity).getJumpStrength();
            break;
         case 4:
            this.jumpStrength = ((SkeletonHorse)livingEntity).getJumpStrength();
            break;
         case 5:
            this.jumpStrength = ((Donkey)livingEntity).getJumpStrength();
            break;
         case 6:
            this.jumpStrength = ((Mule)livingEntity).getJumpStrength();
            break;
         case 7:
            this.jumpStrength = ((Llama)livingEntity).getJumpStrength();
            break;
         default:
            throw new IllegalArgumentException("Player riding " + livingEntity.getType());
      }

      switch (EntityTypeSwitch.ordinalSwitch[this.entityType.ordinal()]) {
         case 1:
            this.stepHeight = 0.6;
            break;
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
            this.stepHeight = 1.0;
            break;
         default:
            throw new IllegalArgumentException("Player riding " + livingEntity.getType());
      }

      if (livingEntity instanceof Player
         && ((Player)livingEntity).getInventory().getBoots() != null
         && ((Player)livingEntity).getInventory().getBoots().getEnchantmentLevel(Enchantment.DEPTH_STRIDER) > 0) {
         this.depthStriderLevel = ((Player)livingEntity).getInventory().getBoots().getEnchantmentLevel(Enchantment.DEPTH_STRIDER);
      } else {
         this.depthStriderLevel = 0;
      }

      this.blockFriction = PlayerStateAccess.getBlockSlipperiness(location.clone().subtract(0.0, 1.0, 0.0).getBlock());
      BoundingBox box = new BoundingBox(location.toVector(), EntityDimensions.STANDING_PLAYER)
         .expandTowards(vector)
         .expand(0.5, 0.601, 0.5);
      BoundingBox box2 = box;
      LivingEntity livingEntity2 = livingEntity;
      List list = CollisionBoxAccess.getCubes(livingEntity2, box2);
      World world = livingEntity.getWorld();
      List list2 = list;
      World world2 = world;
      ProtocolVersion protocolVersion2 = protocolVersion;
      this.collisionBoxes = CollisionResolver.refineBlockBoxes(protocolVersion2, world2, list2);
      if (livingEntity instanceof Player) {
         if (flag) {
            this.boundingBox = new BoundingBox(location.toVector(), (this.pose = PlayerPose.FALL_FLYING).getDimensions(protocolVersion));
         } else if (protocolVersion.isAtLeast(ProtocolVersion.V1_13)) {
            if (!PlayerPose.SWIMMING.canFit(protocolVersion, location.toVector(), this.collisionBoxes)) {
               if (entityPhysicsContext == null) {
                  this.boundingBox = new BoundingBox(location.toVector(), (this.pose = PlayerPose.STANDING).getDimensions(protocolVersion));
               } else {
                  this.pose = entityPhysicsContext.getPose();
                  this.boundingBox = new BoundingBox(location.toVector(), this.pose.getDimensions(protocolVersion));
               }
            } else if ((
                  entityPhysicsContext == null
                     || !entityPhysicsContext.isInWater()
                     || !((Player)livingEntity).isSprinting()
                     || entityPhysicsContext.getPose() != PlayerPose.SWIMMING
                        && !MaterialGroup.WATER.contains(location.clone().add(0.0, livingEntity.getEyeHeight(), 0.0).getBlock().getType())
               )
               && PlayerPose.SNEAKING.canFit(protocolVersion, location.toVector(), this.collisionBoxes)) {
               if (!flag2 && PlayerPose.STANDING.canFit(protocolVersion, location.toVector(), this.collisionBoxes)) {
                  this.boundingBox = new BoundingBox(location.toVector(), (this.pose = PlayerPose.STANDING).getDimensions(protocolVersion));
               } else {
                  this.boundingBox = new BoundingBox(location.toVector(), (this.pose = PlayerPose.SNEAKING).getDimensions(protocolVersion));
               }
            } else {
               this.boundingBox = new BoundingBox(location.toVector(), (this.pose = PlayerPose.SWIMMING).getDimensions(protocolVersion));
            }
         } else if (flag2) {
            this.boundingBox = new BoundingBox(location.toVector(), (this.pose = PlayerPose.SNEAKING).getDimensions(protocolVersion));
         } else {
            this.boundingBox = new BoundingBox(location.toVector(), (this.pose = PlayerPose.STANDING).getDimensions(protocolVersion));
         }
      } else {
         this.pose = PlayerPose.STANDING;
         Vector vector2 = location.toVector();
         LivingEntity livingEntity3 = livingEntity;
         this.boundingBox = new BoundingBox(vector2, EntityDimensions.forEntity(livingEntity3));
      }

      Block block2 = location.getBlock();
      Block block3 = block2.getRelative(BlockFace.DOWN);
      Material material = block2.getType();
      if (material != Material.LADDER
         && material != Material.VINE
         && (!protocolVersion.isAtLeast(ProtocolVersion.V1_14) || !ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_14) || material != Material.SCAFFOLDING)) {
         if (!protocolVersion.isAtLeast(ProtocolVersion.V1_9) || !MaterialGroup.TRAPDOORS.contains(material) || block3.getType() != Material.LADDER) {
            this.climbable = false;
         } else if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)) {
            TrapDoor trapDoor = (TrapDoor)block2.getBlockData();
            Ladder ladder = (Ladder)block3.getBlockData();
            this.climbable = trapDoor.isOpen() && ladder.getFacing() == trapDoor.getFacing();
         } else {
            org.bukkit.material.TrapDoor trapDoor2 = (org.bukkit.material.TrapDoor)block2.getState().getData();
            org.bukkit.material.Ladder ladder2 = (org.bukkit.material.Ladder)block3.getState().getData();
            this.climbable = trapDoor2.isOpen() && trapDoor2.getFacing() == ladder2.getFacing();
         }
      } else {
         this.climbable = true;
      }

      this.inCobweb = CollisionBoxAccess.anyBlockMatches(
         livingEntity.getWorld(), this.boundingBox.expand(-0.001, -0.001, -0.001), block -> block.getType() == MaterialGroup.COBWEB.getMaterial()
      );
      this.inBerryBush = ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_14)
         && protocolVersion.isAtLeast(ProtocolVersion.V1_14)
         && CollisionBoxAccess.anyBlockMatches(livingEntity.getWorld(), this.boundingBox.expand(-0.001, -0.001, -0.001), block -> block.getType() == Material.SWEET_BERRY_BUSH);
      BoundingBox y = this.boundingBox.withMinY(this.boundingBox.getMin().getY() + 0.0625).expand(-0.001, -0.001, -0.001);
      this.touchingHoney = ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_15)
         && protocolVersion.isAtLeast(ProtocolVersion.V1_15)
         && CollisionBoxAccess.anyBlockMatches(livingEntity.getWorld(), y, block -> block.getType() == Material.HONEY_BLOCK);
      this.inBubbleColumn = ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)
         && CollisionBoxAccess.anyBlockMatches(livingEntity.getWorld(), this.boundingBox, block -> block.getType() == Material.BUBBLE_COLUMN);
      this.torsoInLiquid = location.clone().add(0.0, 0.9, 0.0).getBlock().isLiquid();
      BoundingBox box3 = this.boundingBox.expand(-0.001, -0.001, -0.001);
      if (!protocolVersion.isAtLeast(ProtocolVersion.V1_13)) {
         box3 = box3.expand(0.0, -0.4, 0.0);
      }

      this.waterContact = PlayerStateAccess.checkWaterContact(livingEntity.getWorld(), box3, protocolVersion);
      if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)) {
         box3 = this.boundingBox.expand(-0.001, -0.001, -0.001);
      } else {
         box3 = this.boundingBox.expand(-0.1F, -0.4F, -0.1F);
      }

      this.inLava = CollisionBoxAccess.anyBlockMatches(livingEntity.getWorld(), box3, block -> MaterialGroup.LAVA.contains(block.getType()));
      ArrayList list3 = new ArrayList();

      for (Entity entity : livingEntity.getNearbyEntities(6.0, 6.0, 6.0)) {
         if (entity.getType() == EntityType.BOAT) {
            Vector vector3 = entity.getLocation().toVector();
            Entity entity2 = entity;
            list3.add(new BoundingBox(vector3, EntityDimensions.forEntity(entity2)));
         }
      }

      if (protocolVersion.isAtLeast(ProtocolVersion.V1_9)) {
         this.collisionBoxes.addAll(list3);
      }

      Location location2 = location.clone().add(vector).subtract(0.0, 0.2, 0.0);
      Material material2 = location2.getBlock().getType();
      this.slimeBlockPosition = material2 == Material.SLIME_BLOCK ? location2.toVector().toBlockVector() : null;
      this.bedBlockPosition = MaterialGroup.BEDS.contains(material2) ? location2.toVector().toBlockVector() : null;
      Material material3 = location.clone().subtract(0.0, 0.5, 0.0).getBlock().getType();
      if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_15)) {
         this.soulSandContacts = material3 == Material.SOUL_SAND ? 1 : 0;
      } else {
         this.soulSandContacts = CollisionBoxAccess.countMatchingBlocks(
            livingEntity.getWorld(), this.boundingBox.expand(-0.001, -0.001, -0.001), block -> block.getType() == Material.SOUL_SAND
         );
      }

      this.onHoney = ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_15)
         && protocolVersion.isAtLeast(ProtocolVersion.V1_15)
         && material3 == Material.HONEY_BLOCK;
      BoundingBox box4 = this.boundingBox.translate(vector).expand(0.0, 1.0, 0.0);
      int x = (int)Math.floor(box4.getMin().getX());
      int y2 = (int)Math.floor(box4.getMin().getY());
      int z = (int)Math.floor(box4.getMin().getZ());
      int x2 = (int)Math.floor(box4.getMax().getX() + 1.0);
      int y3 = (int)Math.floor(box4.getMax().getY() + 1.0);
      int z2 = (int)Math.floor(box4.getMax().getZ() + 1.0);

      for (int value5 = x; value5 < x2; value5++) {
         for (int value6 = y2; value6 < y3; value6++) {
            for (int value7 = z; value7 < z2; value7++) {
               if (livingEntity.getWorld().getBlockAt(value5, value6, value7).isLiquid()) {
                  this.sampledLiquidBlocks.add(new BlockVector(value5, value6, value7));
               }
            }
         }
      }
   }

   @Override
   public EntityType getEntityType() {
      return this.entityType;
   }

   @Override
   public boolean hasSlowFalling() {
      return this.slowFalling;
   }

   @Override
   public float getBaseMovementSpeed() {
      return this.baseMovementSpeed;
   }

   @Override
   public boolean hasLevitation() {
      return this.levitationAmplifier >= 0;
   }

   @Override
   public int getLevitationAmplifier() {
      return this.levitationAmplifier;
   }

   @Override
   public boolean hasJumpBoost() {
      return this.jumpBoostAmplifier >= 0;
   }

   @Override
   public int getJumpBoostAmplifier() {
      return this.jumpBoostAmplifier;
   }

   @Override
   public boolean isInWater() {
      return this.waterContact.intersectsWater;
   }

   @Override
   public boolean isInLava() {
      return this.inLava;
   }

   @Override
   public double getWaterLevel() {
      return this.waterContact.immersionDepth;
   }

   @Override
   public int getDepthStriderLevel() {
      return this.depthStriderLevel;
   }

   @Override
   public boolean hasDolphinsGrace() {
      return this.dolphinsGrace;
   }

   @Override
   public float getBlockFriction() {
      return this.blockFriction;
   }

   @Override
   public double getJumpStrength() {
      return this.jumpStrength;
   }

   @Override
   public double getStepHeight() {
      return this.stepHeight;
   }

   @Override
   public boolean isClimbable() {
      return this.climbable;
   }

   @Override
   public boolean isInCobweb() {
      return this.inCobweb;
   }

   @Override
   public boolean isInBerryBush() {
      return this.inBerryBush;
   }

   @Override
   public boolean isTouchingHoney() {
      return this.touchingHoney;
   }

   @Override
   public boolean isInBubbleColumn() {
      return this.inBubbleColumn;
   }

   @Override
   public boolean isTorsoInLiquid() {
      return this.torsoInLiquid;
   }

   @Override
   public boolean intersectsLiquidAtOffset(double d, double d2, double d3) {
      BoundingBox box = this.boundingBox.translate(new Vector(d, d2, d3));
      int x = (int)Math.floor(box.getMin().getX());
      int y = (int)Math.floor(box.getMin().getY());
      int z = (int)Math.floor(box.getMin().getZ());
      int x2 = (int)Math.floor(box.getMax().getX() + 1.0);
      int y2 = (int)Math.floor(box.getMax().getY() + 1.0);
      int z2 = (int)Math.floor(box.getMax().getZ() + 1.0);

      for (int value = x; value < x2; value++) {
         for (int value2 = y; value2 < y2; value2++) {
            for (int value3 = z; value3 < z2; value3++) {
               if (this.sampledLiquidBlocks.contains(new BlockVector(value, value2, value3))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   @Override
   public boolean isSlimeBelow(Vector vector) {
      return vector.clone().subtract(new Vector(0.0, 0.2, 0.0)).toBlockVector().equals(this.slimeBlockPosition);
   }

   @Override
   public boolean isBedBelow(Vector vector) {
      return vector.clone().subtract(new Vector(0.0, 0.2, 0.0)).toBlockVector().equals(this.bedBlockPosition);
   }

   @Override
   public int getSoulSandContacts() {
      return this.soulSandContacts;
   }

   @Override
   public boolean isOnHoney() {
      return this.onHoney;
   }

   @Override
   public BoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   @Override
   public PlayerPose getPose() {
      return this.pose;
   }

   @Override
   public List<BoundingBox> getIntersectingCollisionBoxes(BoundingBox box) {
      MotionPrediction.unusedPredictionFlag();
      ArrayList<BoundingBox> list = new ArrayList<>();

      for (BoundingBox box2 : this.collisionBoxes) {
         if (box2.intersects(box)) {
            list.add(box2);
         }
      }

      return list;
   }

   @Override
   public boolean isMovementCollisionFree(BoundingBox box, double d, double d2, double d3) {
      MotionPrediction.unusedPredictionFlag();

      for (BoundingBox box2 : this.collisionBoxes) {
         if (box2.intersectsWithOffset(box, d, d2, d3)) {
            return false;
         }
      }

      return true;
   }
}
