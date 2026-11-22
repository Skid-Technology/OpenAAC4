package me.konsolas.aac.checks.movement;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.compat.CollisionBoxAccess;
import me.konsolas.aac.compat.PlayerStateAccess;
import me.konsolas.aac.compat.ProtocolVersion;
import me.konsolas.aac.movement.EntityDimensions;
import me.konsolas.aac.movement.EntityPhysicsContext;
import me.konsolas.aac.movement.EntityPhysicsSnapshot;
import me.konsolas.aac.movement.EntityPushCalculator;
import me.konsolas.aac.movement.MotionPrediction;
import me.konsolas.aac.movement.MovementContext;
import me.konsolas.aac.movement.MovementInput;
import me.konsolas.aac.movement.MovementPredictor;
import me.konsolas.aac.movement.MovementResolution;
import me.konsolas.aac.movement.OffsetEntityPhysics;
import me.konsolas.aac.movement.PlayerPose;
import me.konsolas.aac.movement.VelocityState;
import me.konsolas.aac.movement.WaterContactState;
import me.konsolas.aac.util.BoundingBox;
import me.konsolas.aac.util.EnvironmentBlockChecks;
import me.konsolas.aac.util.MaterialGroup;
import me.konsolas.aac.util.VectorFormatter;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.konsolas.aac.api.HackType;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Move extends Check {
   public Move(CheckManager checkManager) {
      super(
         checkManager,
         HackType.MOVE,
         MovePlayerData::new,
         Collections.singletonList(Client.HELD_ITEM_SLOT),
         true,
         (player, data) -> System.currentTimeMillis() < ((MovePlayerData)data).lastPredictionFailureMillis + 100L
      );
   }

   private static <T> Predicate<T> distinctByKey(Function<? super T, ?> function) {
      HashSet<Object> set = new HashSet<>();
      return obj -> set.add(function.apply(obj));
   }

   @Override
   public void tick(long currentTick) {
      super.tick(currentTick);
      if (this.getBooleanOption("velocity.check_stationary")) {
         long ticks = this.getLongOption("velocity.wait_ticks");
         this.forAllPlayers((player, checkData) -> {
            MovePlayerData data2 = (MovePlayerData)checkData;
            if (data2.stationaryPending && data2.lastMoveTick + ticks + 1L == currentTick) {
               this.manager.onPlayerMove(new PlayerMoveEvent(player, player.getLocation(), player.getLocation()));
            }
         });
      }
   }

   @Override
   public void onPacket(Player player, PlayerData playerData, PacketEvent event) {
      MovePlayerData data2 = (MovePlayerData)playerData.getCheckData(HackType.MOVE);
      if (event.getPacketType() == Client.HELD_ITEM_SLOT) {
         data2.teleportExempt = true;
      }
   }

   @Override
   public void onRiptide(Player player, PlayerData playerData, int i) {
      MovePlayerData data2 = (MovePlayerData)playerData.getCheckData(HackType.MOVE);
      data2.riptideTicks = 3;
   }

   @Override
   public void onVelocity(Player player, PlayerData playerData, Vector vector) {
      MovePlayerData data2 = (MovePlayerData)playerData.getCheckData(HackType.MOVE);
      data2.pendingVelocity = new VelocityState(vector, false);
      String str = "velocity.enabled";
      if (this.getBooleanOption(str)) {
         data2.lastLocation = player.getLocation();
         data2.stationaryPending = true;
         data2.lastMoveTick = this.manager.getCurrentTick();
      }
   }

   @Override
   public void onTeleport(Player player, PlayerData playerData, Location location) {
      MovePlayerData data2 = (MovePlayerData)playerData.getCheckData(HackType.MOVE);
      data2.exemptMove = true;
   }

   @Override
   public MovementResolution move(Player player, PlayerData playerData, MovementTransition movementTransition) {
      MovePlayerData data2 = (MovePlayerData)playerData.getCheckData(HackType.MOVE);
      if (data2.exemptMove) {
         data2.exemptMove = false;
         data2.pendingResolution = null;
         data2.predictions = null;
         data2.stationaryPending = false;
         data2.lastLocation = null;
         data2.lastMoveTick = 0L;
      }

      if (data2.pendingResolution != null) {
         MovementResolution movementResolution = data2.pendingResolution;
         data2.pendingResolution = null;
         return movementResolution;
      }

      String str = "async";
      if (this.getBooleanOption(str) && data2.analysisThread == null) {
         MovePlayerData data3 = data2;
         Player player2 = player;
         Move move = this;
         data2.analysisThread = new MoveAnalysisThread(move, player2, data3);
      }

      data2.lastPredictionFailureMillis = System.currentTimeMillis();
      Vector vector = movementTransition.getTo().toVector().subtract(movementTransition.getFrom().toVector());
      if (vector.lengthSquared() > 100.0) {
         String str2 = String.format(Locale.UK, "moved too quickly (%.2f)", vector.length());
         short s = 1000;
         Player player3 = player;
         return this.addViolation(player3, s, str2) ? MovementResolution.setback() : null;
      }

      boolean flag = data2.initialized;
      data2.initialized = player.getVehicle() != null;
      if (flag == data2.initialized || !(vector.lengthSquared() <= 16.0) && playerData.getClientVersion().isAtLeast(ProtocolVersion.V1_9)) {
         if (data2.initialized && !playerData.getClientVersion().isAtLeast(ProtocolVersion.V1_9)) {
            data2.predictions = Collections.emptyList();
            return null;
         }

         if (data2.stationaryPending) {
            if (data2.pendingVelocity != null
               && playerData.getMovementState().pendingKnockback != null
               && player.getVehicle() == null
               && !EnvironmentBlockChecks.isInCobweb(movementTransition.getFromBlockAccess())
               && !EnvironmentBlockChecks.isOnClimbable(movementTransition.getFromBlockAccess())) {
               long ticks = this.manager.getCurrentTick() - data2.lastMoveTick;
               String str3 = "velocity.wait_ticks";
               if (ticks > this.getLongOption(str3)) {
                  data2.stationaryPending = false;
                  data2.lastLocation = null;
                  data2.lastMoveTick = 0L;
                  String str4 = "velocity.vl_increment";
                  int value = this.getIntOption(str4);
                  StringBuilder sb = new StringBuilder().append("ignored velocity of ");
                  Vector vector2 = data2.pendingVelocity.getVelocity();
                  String str5 = sb.append(VectorFormatter.formatVector(vector2)).toString();
                  int value2 = value;
                  Player player4 = player;
                  if (this.addViolation(player4, value2, str5)) {
                     String str6 = "velocity.wait_ticks";
                     return new MovementResolution(data2.lastLocation, this.getLongOption(str6), null);
                  }
               }
            } else {
               data2.stationaryPending = false;
               data2.lastLocation = null;
               data2.lastMoveTick = 0L;
            }
         }

         if (player.getVehicle() == null || player.getVehicle() instanceof LivingEntity) {
            boolean flag2 = PlayerStateAccess.isGliding(player);
            boolean flag3 = player.isSneaking();
            boolean flag4 = !data2.teleportExempt && PlayerStateAccess.isUsingItem(player);
            if (flag2 && ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_11)) {
               for (Entity entity : player.getNearbyEntities(1.0, 1.0, 1.0)) {
                  if (entity.getType() == EntityType.FIREWORK) {
                     data2.lastFirework = new WeakReference<>(entity);
                  }
               }
            }

            EntityPhysicsSnapshot entityPhysicsSnapshot;
            try {
               EntityPhysicsContext entityPhysicsContext = data2.lastPhysicsContext;
               Object obj = player.getVehicle() != null ? (LivingEntity)player.getVehicle() : player;
               Location location = movementTransition.getFrom();
               ProtocolVersion protocolVersion = playerData.getClientVersion();
               Vector vector3 = vector;
               ProtocolVersion protocolVersion2 = protocolVersion;
               boolean flag5 = flag3;
               boolean flag6 = flag2;
               Location location2 = location;
               Object obj2 = obj;
               EntityPhysicsContext entityPhysicsContext2 = entityPhysicsContext;
               entityPhysicsSnapshot = new EntityPhysicsSnapshot(entityPhysicsContext2, (LivingEntity)obj2, location2, flag6, flag5, protocolVersion2, vector3);
            } catch (IllegalArgumentException ex) {
               return null;
            }

            data2.lastPhysicsContext = entityPhysicsSnapshot;
            Boolean boolValue = null;
            if (flag2 == data2.usingItem) {
               boolValue = flag2;
            }

            data2.usingItem = flag2;
            Boolean boolValue2 = null;
            flag3 |= entityPhysicsSnapshot.getPose() == PlayerPose.SNEAKING;
            if (flag3 == data2.sprinting) {
               boolValue2 = flag3;
            }

            data2.sprinting = flag3;
            Boolean boolValue3 = null;
            if (flag4 == data2.wasSprinting) {
               boolValue3 = flag4;
            }

            data2.wasSprinting = flag4;
            data2.teleportExempt = false;
            WaterContactState waterContactState = PlayerStateAccess.checkWaterContact(player.getWorld(), entityPhysicsSnapshot.getBoundingBox(), ProtocolVersion.getServerVersion());
            float pitch = movementTransition.getTo().getPitch();
            float yaw = movementTransition.getTo().getYaw();
            List<Entity> list = player.getNearbyEntities(12.0, 12.0, 12.0);
            boolean flag7 = !list.isEmpty();
            boolean flag8 = player.getFoodLevel() > 6 && !player.hasPotionEffect(PotionEffectType.BLINDNESS);
            boolean flag9 = ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_11)
               && CollisionBoxAccess.anyBlockMatches(
                  player.getWorld(), entityPhysicsSnapshot.getBoundingBox().expandTowards(new Vector(0.0, -1.11, 0.0)), block -> MaterialGroup.SHULKER_BOXES.contains(block.getType())
               );
            boolean flag10 = list.stream().anyMatch(entity2 -> entity2.getType() == EntityType.BOAT && movementTransition.getFrom().distanceSquared(entity2.getLocation()) < 9.0);
            double d = EntityPushCalculator.calculatePushVector(player).magnitude();
            boolean flag11;
            if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_9)) {
               flag11 = player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.KNOCKBACK) > 0;
            } else {
               flag11 = player.getItemInHand().getEnchantmentLevel(Enchantment.KNOCKBACK) > 0;
            }

            MovementContext movementContext = new MovementContext(
               player, playerData, movementTransition, data2, vector, boolValue, boolValue2, boolValue3, entityPhysicsSnapshot, waterContactState, pitch, yaw, flag11, flag7, flag8, flag9, flag10, d);
            if (data2.analysisThread != null) {
               try {
                  MoveAnalysisThread.getPendingQueue(data2.analysisThread).put(movementContext);
                  return null;
               } catch (InterruptedException ex2) {
                  throw new RuntimeException(ex2);
               }
            } else {
               return this.getSetbackRequest(movementContext);
            }
         } else if (player.getVehicle() instanceof Boat && ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_9)) {
            data2.predictions = Collections.emptyList();
            Vector vector4 = player.getVehicle().getLocation().toVector();
            Entity entity3 = player.getVehicle();
            BoundingBox box = new BoundingBox(vector4, EntityDimensions.forEntity(entity3)).expand(1.0, 0.1, 1.0);
            boolean flag12 = CollisionBoxAccess.anyBlockMatches(player.getWorld(), box, block2 -> MaterialGroup.ICE.contains(block2.getType()));
            double d2 = flag12 ? 4.0 : 0.4;
            if (!player.getVehicle().isOnGround() && !player.getVehicle().getLocation().getBlock().isLiquid() && vector.getY() > 0.0) {
               StringBuilder sb2 = new StringBuilder().append("invalid movement: ");
               Vector vector5 = vector;
               sb2 = sb2.append(VectorFormatter.formatVector(vector5)).append(" deltaY: ");
               double y = vector.getY();
               String str7 = sb2.append(VectorFormatter.formatDouble(y))
                  .append(" ground: ")
                  .append(movementTransition.getFromGroundState())
                  .append(movementTransition.getToGroundState())
                  .append(" type: BOAT")
                  .toString();
               byte b = 10;
               Player player5 = player;
               return this.addViolation(player5, b, str7) ? MovementResolution.setback() : null;
            } else {
               double d3 = vector.lengthSquared();
               double d4 = d2 * d2;
               String str8 = "vehicle.tolerance";
               if (d3 > d4 + this.getDoubleOption(str8)) {
                  StringBuilder sb3 = new StringBuilder().append("invalid movement: ");
                  Vector vector6 = vector;
                  sb3 = sb3.append(VectorFormatter.formatVector(vector6)).append(" length: ");
                  double d5 = vector.length();
                  String str9 = sb3.append(VectorFormatter.formatDouble(d5))
                     .append(" ground: ")
                     .append(movementTransition.getFromGroundState())
                     .append(movementTransition.getToGroundState())
                     .append(" type: BOAT")
                     .toString();
                  byte b2 = 10;
                  Player player6 = player;
                  return this.addViolation(player6, b2, str9) ? MovementResolution.setback() : null;
               } else {
                  return null;
               }
            }
         } else {
            return null;
         }
      } else {
         data2.predictions = Collections.emptyList();
         return null;
      }
   }

   private MovementResolution getSetbackRequest(MovementContext movementContext) {
      MovementPredictor movementPredictor = new MovementPredictor(movementContext.getPhysics(), movementContext.getTransition().getFrom().toVector(), movementContext.getPlayerData().getClientVersion());
      if (movementContext.getCheckData().predictions == null) {
         movementContext.getCheckData().predictions = Collections.singletonList(
            movementPredictor.createInitialPrediction(
               movementContext.getTransition().getFrom(),
               movementContext.getTransition().getFromGroundState(),
               movementContext.getTransition().getFromGroundState(),
               movementContext.getPlayer().isSprinting(),
               movementContext.getPlayerData().getMovementState().lastVelocity
            )
         );
      } else if (movementContext.getCheckData().predictions.isEmpty()) {
         if (movementContext.getMovement().lengthSquared() < 1.0) {
            movementContext.getCheckData().predictions = Collections.singletonList(
               movementPredictor.createInitialPrediction(
                  movementContext.getTransition().getFrom(),
                  movementContext.getTransition().getFromGroundState(),
                  movementContext.getTransition().getToGroundState(),
                  movementContext.getPlayer().isSprinting(),
                  movementContext.getMovement()
               )
            );
            return null;
         }

         movementContext.getCheckData().predictions = Collections.singletonList(
            movementPredictor.createInitialPrediction(
               movementContext.getTransition().getFrom(),
               movementContext.getTransition().getFromGroundState(),
               movementContext.getTransition().getFromGroundState(),
               movementContext.getPlayer().isSprinting(),
               movementContext.getPlayerData().getMovementState().lastVelocity
            )
         );
      }

      double d;
      boolean flag;
      double d2;
      if (movementContext.getPhysics().getEntityType() != EntityType.PLAYER) {
         String str = "vehicle.tolerance";
         d = this.getDoubleOption(str);
         flag = false;
         String str2 = "vehicle.weight";
         d2 = this.getDoubleOption(str2);
      } else if (movementContext.getWaterContact().intersectsWater) {
         String str3 = "water.tolerance";
         d = this.getDoubleOption(str3);
         flag = true;
         String str4 = "water.weight";
         d2 = this.getDoubleOption(str4);
      } else if (movementContext.getElytraGliding() != null && !movementContext.getElytraGliding()) {
         String str5 = "normal.tolerance";
         d = this.getDoubleOption(str5);
         flag = true;
         String str6 = "normal.weight";
         d2 = this.getDoubleOption(str6);
      } else {
         String str7 = "elytra.tolerance";
         d = this.getDoubleOption(str7);
         flag = true;
         String str8 = "elytra.weight";
         d2 = this.getDoubleOption(str8);
      }

      boolean flag2 = movementPredictor.getClientVersion().isAtLeast(ProtocolVersion.V1_13)
         && movementContext.getCheckData().predictions.stream().anyMatch(motionPrediction -> this.horizontalLengthSquared(motionPrediction.getVectorState().getPositionDelta()) == 0.0);
      ArrayList<MotionPrediction> list = new ArrayList<>();

      for (MotionPrediction motionPrediction2 : movementContext.getCheckData().predictions) {
         list.addAll(this.generatePredictions(movementPredictor, movementContext.isNearShulkerBox() ? motionPrediction2.withOnGround() : motionPrediction2, movementContext, movementContext.getCheckData().pendingVelocity));
         if (movementContext.getCheckData().pendingVelocity != null) {
            list.addAll(this.generatePredictions(movementPredictor, movementContext.isNearShulkerBox() ? motionPrediction2.withOnGround() : motionPrediction2, movementContext, null));
         }
      }

      StringBuilder sb = new StringBuilder();
      if (movementContext.getCheckData().pendingVelocity != null) {
         StringBuilder sb2 = sb.append(" vel: ");
         Vector vector = movementContext.getCheckData().pendingVelocity.getVelocity();
         sb2.append(VectorFormatter.formatVector(vector));
      }

      if (movementContext.getSneaking() == null) {
         sb.append(" sneak?");
      } else if (movementContext.getSneaking()) {
         sb.append(" sneak");
      }

      if (movementContext.getUsingItem() == null) {
         sb.append(" item?");
      } else if (movementContext.getUsingItem()) {
         sb.append(" item");
      }

      if (movementContext.getElytraGliding() == null) {
         sb.append(" elytra?");
      } else if (movementContext.getElytraGliding()) {
         sb.append(" elytra");
      }

      if (movementContext.getPhysics().isClimbable()) {
         sb.append(" ladder");
      }

      if (movementContext.getPhysics().isInWater()) {
         sb.append(" water");
      }

      boolean flag3 = list.stream().anyMatch(motionPrediction3 -> motionPrediction3.getVectorState().getPositionDelta().lengthSquared() < 9.0E-4);
      if (flag3 && movementContext.getMovement().lengthSquared() == 0.0) {
         return null;
      }

      int i;
      boolean flag4;
      movementContext.getCheckData().predictions = this.filterMatchingPredictions(movementContext.getMovement(), d, flag, list);
      i = 0;
      flag4 = false;
      label356:
      if (movementContext.getCheckData().predictions.isEmpty()) {
         if (flag2) {
            sb.append("extX:").append(list.get(0).getControls().getAcceleration());
            movementContext.getCheckData().predictions = list.stream()
               .filter(motionPrediction4 -> this.matchesAxisTolerance(movementContext.getMovement().getY(), motionPrediction4.getVectorState().getPositionDelta().getY(), motionPrediction4.getControls().getAcceleration() * 2.0F + d))
               .filter(motionPrediction5 -> this.matchesAxisTolerance(movementContext.getMovement().getZ(), motionPrediction5.getVectorState().getPositionDelta().getZ(), d))
               .filter(motionPrediction6 -> this.matchesAxisTolerance(movementContext.getMovement().getX(), motionPrediction6.getVectorState().getPositionDelta().getX(), motionPrediction6.getControls().getAcceleration() * 6.0F + d))
               .filter(distinctByKey(MotionPrediction::getControls))
               .map(motionPrediction7 -> motionPrediction7.withActualMovement(movementContext.getMovement()))
               .collect(Collectors.toList());
            if (!movementContext.getCheckData().predictions.isEmpty()) {
               flag4 = true;
               break label356;
            }
         }

         if (movementContext.getPhysics().getEntityType() != EntityType.PLAYER && movementContext.getMovement().getY() > 0.0) {
            sb.append(" vehiclejump");
            if (list.stream().anyMatch(motionPrediction8 -> motionPrediction8.getVectorState().getPositionDelta().getY() + d > movementContext.getMovement().getY())) {
               movementContext.getCheckData().predictions = list.stream()
                  .filter(motionPrediction9 -> motionPrediction9.getVectorState().getPositionDelta().getY() > 0.0)
                  .filter(motionPrediction10 -> !motionPrediction10.getVectorState().isOnGround())
                  .filter(motionPrediction11 -> this.horizontalLength(motionPrediction11.getVectorState().getPositionDelta()) + d > this.horizontalLength(movementContext.getMovement()))
                  .filter(distinctByKey(MotionPrediction::getControls))
                  .map(motionPrediction12 -> motionPrediction12.withActualMovement(movementContext.getMovement()))
                  .collect(Collectors.toList());
               if (!movementContext.getCheckData().predictions.isEmpty()) {
                  flag4 = true;
                  break label356;
               }
            }
         }

         if (movementContext.getWaterContact().intersectsFlowingWater) {
            sb.append(" flowing");
            String str9 = "water.flowing";
            double d3 = Math.pow(this.getDoubleOption(str9), 2.0);
            movementContext.getCheckData().predictions = list.stream()
               .filter(motionPrediction13 -> motionPrediction13.getVectorState().getPositionDelta().distanceSquared(movementContext.getMovement()) < d3)
               .filter(distinctByKey(MotionPrediction::getControls))
               .map(motionPrediction14 -> motionPrediction14.withActualMovement(movementContext.getMovement()))
               .collect(Collectors.toList());
            if (!movementContext.getCheckData().predictions.isEmpty()) {
               flag4 = true;
               break label356;
            }
         }

         if (movementContext.isNearBoat()) {
            sb.append(" boat");
            String str10 = "water.boat";
            double d4 = Math.pow(this.getDoubleOption(str10), 2.0);
            movementContext.getCheckData().predictions = list.stream()
               .filter(motionPrediction15 -> motionPrediction15.getVectorState().getPositionDelta().distanceSquared(movementContext.getMovement()) < d4)
               .filter(distinctByKey(MotionPrediction::getControls))
               .map(motionPrediction16 -> motionPrediction16.withActualMovement(movementContext.getMovement()))
               .collect(Collectors.toList());
            if (!movementContext.getCheckData().predictions.isEmpty()) {
               flag4 = true;
               break label356;
            }
         }

         if (movementContext.getPhysics().isInBubbleColumn()) {
            sb.append(" bubble_column");
            String str11 = "water.bubble_column";
            double d5 = Math.pow(this.getDoubleOption(str11), 2.0);
            movementContext.getCheckData().predictions = list.stream()
               .filter(motionPrediction17 -> motionPrediction17.getVectorState().getPositionDelta().distanceSquared(movementContext.getMovement()) < d5)
               .filter(distinctByKey(MotionPrediction::getControls))
               .map(motionPrediction18 -> motionPrediction18.withActualMovement(movementContext.getMovement()))
               .collect(Collectors.toList());
            if (!movementContext.getCheckData().predictions.isEmpty()) {
               flag4 = true;
               break label356;
            }
         }

         if (movementContext.getElytraGliding() == null || movementContext.getElytraGliding()) {
            Entity entity = movementContext.getCheckData().lastFirework == null ? null : movementContext.getCheckData().lastFirework.get();
            boolean flag5 = entity != null && !entity.isDead();
            if (flag5) {
               movementContext.getCheckData().rocketBoostTicks = 10;
            } else {
               flag5 = movementContext.getCheckData().rocketBoostTicks-- > 0;
            }

            if (flag5) {
               sb.append(" rocket");
               String str12 = "elytra.rocket_speed";
               double d6 = Math.pow(this.getDoubleOption(str12), 2.0);
               movementContext.getCheckData().predictions = list.stream()
                  .filter(motionPrediction19 -> motionPrediction19.getVectorState().getPositionDelta().distanceSquared(movementContext.getMovement()) < d6)
                  .filter(distinctByKey(MotionPrediction::getControls))
                  .map(motionPrediction20 -> motionPrediction20.withActualMovement(movementContext.getMovement()))
                  .collect(Collectors.toList());
               if (!movementContext.getCheckData().predictions.isEmpty()) {
                  flag4 = true;
                  break label356;
               }
            }
         }

         if (movementContext.getCheckData().riptideTicks > 0) {
            movementContext.getCheckData().riptideTicks--;
            String str13 = "water.riptide_speed";
            double d7 = Math.pow(this.getDoubleOption(str13), 2.0);
            movementContext.getCheckData().predictions = list.stream()
               .filter(motionPrediction21 -> motionPrediction21.getVectorState().getPositionDelta().distanceSquared(movementContext.getMovement()) < d7)
               .filter(distinctByKey(MotionPrediction::getControls))
               .map(motionPrediction22 -> motionPrediction22.withActualMovement(movementContext.getMovement()))
               .collect(Collectors.toList());
            if (!movementContext.getCheckData().predictions.isEmpty()) {
               flag4 = true;
               break label356;
            }
         }

         if (movementContext.isNearShulkerBox()) {
            double d8 = Math.pow(0.2, 2.0);
            movementContext.getCheckData().predictions = list.stream()
               .filter(motionPrediction23 -> motionPrediction23.getVectorState().getPositionDelta().distanceSquared(movementContext.getMovement()) < d8)
               .filter(distinctByKey(MotionPrediction::getControls))
               .map(motionPrediction24 -> motionPrediction24.withActualMovement(movementContext.getMovement()))
               .collect(Collectors.toList());
            if (!movementContext.getCheckData().predictions.isEmpty()) {
               flag4 = true;
               break label356;
            }
         }

         if (movementContext.getPhysics().isClimbable()
            && movementContext.getMovement().getY() <= 0.121
            && movementContext.getMovement().getY() >= -0.151
            && Math.abs(movementContext.getMovement().getX()) <= 0.151
            && Math.abs(movementContext.getMovement().getZ()) <= 0.151) {
            movementContext.getCheckData().predictions = list.stream()
               .filter(distinctByKey(MotionPrediction::getControls))
               .map(motionPrediction25 -> motionPrediction25.withActualMovement(movementContext.getMovement()))
               .collect(Collectors.toList());
            flag4 = true;
         } else {
            label411: {
               if (movementContext.getPlayerData().getClientVersion().isAtLeast(ProtocolVersion.V1_9) && movementContext.getEntityPushStrength() > 0.0) {
                  sb.append(String.format(" bump:%.3f", movementContext.getEntityPushStrength()));
                  String str14 = "normal.bumping";
                  double d9 = Math.pow(this.getDoubleOption(str14) + movementContext.getEntityPushStrength() + d, 2.0);
                  movementContext.getCheckData().predictions = list.stream()
                     .filter(motionPrediction26 -> motionPrediction26.getVectorState().getPositionDelta().distanceSquared(movementContext.getMovement()) < d9)
                     .filter(distinctByKey(MotionPrediction::getControls))
                     .map(motionPrediction27 -> motionPrediction27.withActualMovement(movementContext.getMovement()))
                     .collect(Collectors.toList());
                  if (!movementContext.getCheckData().predictions.isEmpty()) {
                     flag4 = true;
                     break label411;
                  }
               }

               Iterator<Entry<BoundingBox, Vector>> iterator = this.manager.getPistonSlimeCache().asMap().entrySet().iterator();

               label297:
               while (true) {
                  if (!iterator.hasNext()) {
                     for (Entry<BoundingBox, Vector> entry : this.manager.getPistonBlockCache().asMap().entrySet()) {
                        if (movementContext.getPhysics().getBoundingBox().intersects(entry.getKey())) {
                           Vector vector2 = entry.getValue();

                           for (MotionPrediction motionPrediction28 : list) {
                              Vector vector3 = movementContext.getMovement().clone().subtract(motionPrediction28.getVectorState().getPositionDelta());
                              if (vector3.length() < 1.0 + d && vector3.angle(vector2) < 0.5) {
                                 Vector vector4 = new Vector(1, 1, 1).subtract(vector2);
                                 movementContext.getCheckData()
                                    .predictions
                                    .add(motionPrediction28.withVelocity(vector4.multiply(motionPrediction28.getVectorState().getVelocity()).add(vector2)));
                              }
                           }
                        }

                        if (!movementContext.getCheckData().predictions.isEmpty()) {
                           flag4 = true;
                           break label297;
                        }
                     }

                     ArrayList<MotionPrediction> list2 = list;

                     while (true) {
                        if (!movementContext.getCheckData().predictions.isEmpty()) {
                           break label297;
                        }

                        i++;
                        if (i > 2 || list2.size() <= 0 || list.size() >= 1024) {
                           break label297;
                        }

                        ArrayList<MotionPrediction> list3 = new ArrayList<>();

                        for (MotionPrediction motionPrediction29 : list2) {
                           if (motionPrediction29.getVectorState().getPositionDelta().lengthSquared() < 9.0E-4) {
                              MovementPredictor movementPredictor2 = new MovementPredictor(
                                 new OffsetEntityPhysics(movementContext.getPhysics(), motionPrediction29.getVectorState().getPositionDelta()),
                                 movementContext.getTransition().getFrom().toVector().add(motionPrediction29.getVectorState().getPositionDelta()),
                                 movementContext.getPlayerData().getClientVersion()
                              );
                              List<MotionPrediction> list4 = this.generatePredictions(movementPredictor2, motionPrediction29, movementContext, movementContext.getCheckData().pendingVelocity);
                              if (movementContext.getCheckData().pendingVelocity != null) {
                                 list4.addAll(this.generatePredictions(movementPredictor2, motionPrediction29, movementContext, null));
                              }

                              for (MotionPrediction motionPrediction30 : list4) {
                                 list3.add(motionPrediction30.chainOffset(motionPrediction29.getVectorState().getPositionDelta()));
                              }
                           }
                        }

                        list2 = list3;
                        list.addAll(list2);
                        movementContext.getCheckData().predictions = this.filterMatchingPredictions(movementContext.getMovement(), d, flag, list2);
                     }
                  }

                  Entry<BoundingBox, Vector> entry2 = iterator.next();
                  if (movementContext.getPhysics().getBoundingBox().intersects(entry2.getKey())) {
                     Vector vector5 = entry2.getValue();

                     for (MotionPrediction motionPrediction31 : list) {
                        Vector vector6 = movementContext.getMovement().clone().subtract(motionPrediction31.getVectorState().getPositionDelta());
                        if (vector6.length() < 1.0 + d && vector6.angle(vector5) < 0.5) {
                           movementContext.getCheckData().predictions.add(motionPrediction31);
                        }
                     }
                  }

                  if (!movementContext.getCheckData().predictions.isEmpty()) {
                     flag4 = true;
                     break;
                  }
               }
            }
         }
      }

      String str15 = "skipped.min_length";
      double d10 = this.getDoubleOption(str15);
      boolean flag6 = movementContext.getMovement().lengthSquared() > d10 * d10;
      if (movementContext.getCheckData().predictions.isEmpty() && (flag3 || i > 0)) {
         String str16 = "skipped.tolerance";
         double d11 = this.getDoubleOption(str16);
         movementContext.getCheckData().predictions = this.filterMatchingPredictions(movementContext.getMovement(), d11, flag, list);
         if (!movementContext.getCheckData().predictions.isEmpty()) {
            movementContext.getCheckData().predictions.sort(Comparator.comparingDouble(motionPrediction32 -> motionPrediction32.getVectorState().getPositionDelta().distanceSquared(movementContext.getMovement())));
            movementContext.getCheckData().predictions = movementContext.getCheckData().predictions.stream().map(motionPrediction33 -> motionPrediction33.withActualMovement(movementContext.getMovement())).collect(Collectors.toList());
            if (flag6 || movementContext.getMovement().getY() != 0.0) {
               this.manager.getCheck(HackType.TIMER).ifPresent(check -> ((Timer)check).addBalance(movementContext.getPlayerData(), 1));
            }
         }
      }

      MovePlayerData data2 = movementContext.getCheckData();
      Stream<MotionPrediction> stream = movementContext.getCheckData().predictions.stream().distinct().sorted(Comparator.comparingDouble(motionPrediction34 -> motionPrediction34.getVectorState().getPositionDelta().distanceSquared(movementContext.getMovement())));
      String str17 = "simulation_limit";
      data2.predictions = stream.limit(this.getIntOption(str17)).collect(Collectors.toList());
      if (movementContext.getCheckData().predictions.isEmpty()) {
         MotionPrediction motionPrediction35 = Collections.min(list, Comparator.comparingDouble(motionPrediction36 -> motionPrediction36.getVectorState().getPositionDelta().distanceSquared(movementContext.getMovement())));
         double d12 = this.horizontalDistanceSquared(motionPrediction35.getVectorState().getPositionDelta(), movementContext.getMovement());
         double y = Math.pow(motionPrediction35.getVectorState().getPositionDelta().getY() - movementContext.getMovement().getY(), 2.0);
         double d13 = d2 * (d12 * 1000.0 + y * 2000.0);
         String str18 = "max_vl_increment";
         int value = (int)Math.min(d13, this.getIntOption(str18));
         value = Math.max(value, 1);
         long now = System.currentTimeMillis();

         while (!movementContext.getCheckData().violationTimestamps.isEmpty() && movementContext.getCheckData().violationTimestamps.element() < now - 1000L) {
            movementContext.getCheckData().violationTimestamps.poll();
         }

         value += movementContext.getCheckData().violationTimestamps.size();
         movementContext.getCheckData().violationTimestamps.add(now);
         movementContext.getCheckData().stationaryPending = false;
         Player player = movementContext.getPlayer();
         StringBuilder sb3 = new StringBuilder().append("at: ");
         Vector vector7 = movementContext.getTransition().getFrom().toVector();
         sb3 = sb3.append(VectorFormatter.formatVector(vector7)).append(" move: ");
         Vector vector8 = movementContext.getMovement();
         sb3 = sb3.append(VectorFormatter.formatVector(vector8)).append(" pred: ");
         Vector vector9 = motionPrediction35.getVectorState().getPositionDelta();
         sb3 = sb3.append(VectorFormatter.formatVector(vector9)).append(" dist: ");
         double d14 = motionPrediction35.getVectorState().getPositionDelta().distance(movementContext.getMovement());
         String str19 = sb3.append(VectorFormatter.formatDouble(d14))
            .append(" info: ")
            .append(movementContext.getTransition().getFromGroundState())
            .append(movementContext.getTransition().getToGroundState())
            .append(" ")
            .append(movementContext.getPhysics().getEntityType().name())
            .append(" ")
            .append(movementContext.getPlayerData().getClientVersion())
            .append(sb.toString())
            .toString();
         int value2 = value;
         Player player2 = player;
         if (this.addViolation(player2, value2, str19)) {
            Vector vector10 = movementContext.getCheckData().pendingVelocity != null
               ? movementContext.getCheckData().pendingVelocity.getVelocity()
               : motionPrediction35.getVectorState().getPositionDelta().clone().multiply(0.546);
            return new MovementResolution(movementContext.getTransition().getFrom(), 1.0, vector10);
         } else {
            movementContext.getCheckData().predictions = Collections.singletonList(
               movementPredictor.createInitialPrediction(
                  movementContext.getTransition().getFrom(),
                  movementContext.getTransition().getFromGroundState(),
                  movementContext.getTransition().getToGroundState(),
                  movementContext.getPlayer().isSprinting(),
                  movementContext.getMovement()
               )
            );
            return null;
         }
      } else {
         if (flag6 || movementContext.getMovement().getY() != 0.0) {
            this.manager.getCheck(HackType.TIMER).ifPresent(check2 -> ((Timer)check2).addBalance(movementContext.getPlayerData(), movementContext.getCheckData().predictions.get(0).getDepth()));
         }

         if (movementContext.getCheckData().predictions.stream().allMatch(motionPrediction37 -> motionPrediction37.getVelocityState() != null) && !flag4) {
            movementContext.getCheckData().pendingVelocity = null;
         }

         if (movementContext.getCheckData().stationaryPending && movementContext.getCheckData().predictions.stream().anyMatch(motionPrediction38 -> motionPrediction38.getVelocityState() != null)) {
            movementContext.getCheckData().stationaryPending = false;
         }

         return null;
      }
   }

   private List<MotionPrediction> generatePredictions(MovementPredictor movementPredictor, MotionPrediction motionPrediction, MovementContext movementContext, VelocityState velocityState) {
      boolean flag = motionPrediction.getVectorState().isOnGround()
         || motionPrediction.getControls().isInWater()
         || movementPredictor.getContext().isInWater()
         || motionPrediction.getControls().isInLava()
         || movementPredictor.getContext().isInLava();
      boolean x = motionPrediction.getVectorState().getVelocity().getX() != 0.0 && motionPrediction.getVectorState().getVelocity().getZ() != 0.0;
      boolean flag2 = movementContext.hasNearbyEntities() && x;
      ArrayList<MotionPrediction> list = new ArrayList<>(36);
      boolean flag3 = movementContext.getSneaking() == null ? false : movementContext.getSneaking();
      boolean flag4 = movementContext.getUsingItem() == null ? false : movementContext.getUsingItem();
      boolean flag5 = movementContext.getElytraGliding() == null ? false : movementContext.getElytraGliding();
      this.populateDirectionalInputs(
         movementPredictor, motionPrediction, movementContext.getPitch(), movementContext.getYaw(), movementContext.canSprint(), velocityState, flag, flag2, movementContext.hasKnockbackItem(), list, flag3, flag4, flag5
      );
      if (movementContext.getSneaking() == null) {
         this.populateDirectionalInputs(
            movementPredictor,
            motionPrediction,
            movementContext.getPitch(),
            movementContext.getYaw(),
            movementContext.canSprint(),
            velocityState,
            flag,
            flag2,
            movementContext.hasKnockbackItem(),
            list,
            !flag3,
            flag4,
            flag5
         );
      }

      if (movementContext.getUsingItem() == null) {
         this.populateDirectionalInputs(
            movementPredictor,
            motionPrediction,
            movementContext.getPitch(),
            movementContext.getYaw(),
            movementContext.canSprint(),
            velocityState,
            flag,
            flag2,
            movementContext.hasKnockbackItem(),
            list,
            flag3,
            !flag4,
            flag5
         );
      }

      if (movementContext.getUsingItem() == null && movementContext.getSneaking() == null) {
         this.populateDirectionalInputs(
            movementPredictor,
            motionPrediction,
            movementContext.getPitch(),
            movementContext.getYaw(),
            movementContext.canSprint(),
            velocityState,
            flag,
            flag2,
            movementContext.hasKnockbackItem(),
            list,
            !flag3,
            !flag4,
            flag5
         );
      }

      if (movementContext.getElytraGliding() == null) {
         this.populateDirectionalInputs(
            movementPredictor,
            motionPrediction,
            movementContext.getPitch(),
            movementContext.getYaw(),
            movementContext.canSprint(),
            velocityState,
            flag,
            flag2,
            movementContext.hasKnockbackItem(),
            list,
            flag3,
            flag4,
            !flag5
         );
      }

      return list;
   }

   private void populateDirectionalInputs(
      MovementPredictor movementPredictor,
      MotionPrediction motionPrediction,
      float f,
      float f2,
      boolean flag,
      VelocityState velocityState,
      boolean flag2,
      boolean flag3,
      boolean flag4,
      List list,
      boolean flag5,
      boolean flag6,
      boolean flag7
   ) {

      for (int value = 1; value >= -1; value--) {
         for (int value2 = -1; value2 <= 1; value2++) {
            list.add(movementPredictor.predict(motionPrediction, new MovementInput(value, value2, false, false, flag5, flag6, flag7, f, f2, 0), velocityState));
            if (value >= 0 && (value > 0 || !motionPrediction.getControls().isSlowedBySprint()) && flag) {
               list.add(movementPredictor.predict(motionPrediction, new MovementInput(value, value2, true, false, flag5, flag6, flag7, f, f2, 0), velocityState));
            }

            if (flag2) {
               list.add(movementPredictor.predict(motionPrediction, new MovementInput(value, value2, false, true, flag5, flag6, flag7, f, f2, 0), velocityState));
            }

            if (value >= 0 && (value > 0 || !motionPrediction.getControls().isSlowedBySprint()) && flag && flag2) {
               list.add(movementPredictor.predict(motionPrediction, new MovementInput(value, value2, true, true, flag5, flag6, flag7, f, f2, 0), velocityState));
            }

            if (flag3) {
               list.add(movementPredictor.predict(motionPrediction, new MovementInput(value, value2, false, false, flag5, flag6, flag7, f, f2, 1), velocityState));
               if (value >= 0 && (value > 0 || !motionPrediction.getControls().isSlowedBySprint()) && flag) {
                  list.add(movementPredictor.predict(motionPrediction, new MovementInput(value, value2, true, false, flag5, flag6, flag7, f, f2, 1), velocityState));
               }

               if (flag2) {
                  list.add(movementPredictor.predict(motionPrediction, new MovementInput(value, value2, false, true, flag5, flag6, flag7, f, f2, 1), velocityState));
               }

               if (value >= 0 && (value > 0 || !motionPrediction.getControls().isSlowedBySprint()) && flag && flag2) {
                  list.add(movementPredictor.predict(motionPrediction, new MovementInput(value, value2, true, true, flag5, flag6, flag7, f, f2, 1), velocityState));
               }
            }

            if (flag3 && flag4) {
               list.add(movementPredictor.predict(motionPrediction, new MovementInput(value, value2, false, false, flag5, flag6, flag7, f, f2, 2), velocityState));
               if (value >= 0 && (value > 0 || !motionPrediction.getControls().isSlowedBySprint()) && flag) {
                  list.add(movementPredictor.predict(motionPrediction, new MovementInput(value, value2, true, false, flag5, flag6, flag7, f, f2, 2), velocityState));
               }

               if (flag2) {
                  list.add(movementPredictor.predict(motionPrediction, new MovementInput(value, value2, false, true, flag5, flag6, flag7, f, f2, 2), velocityState));
               }

               if (value >= 0 && (value > 0 || !motionPrediction.getControls().isSlowedBySprint()) && flag && flag2) {
                  list.add(movementPredictor.predict(motionPrediction, new MovementInput(value, value2, true, true, flag5, flag6, flag7, f, f2, 2), velocityState));
               }
            }
         }
      }
   }

   private List<MotionPrediction> filterMatchingPredictions(Vector vector, double d, boolean flag, List<MotionPrediction> list) {
      double d2 = d * d;
      if (flag) {
         ArrayList<MotionPrediction> list2 = new ArrayList<>();

         for (MotionPrediction motionPrediction : list) {
            if (vector.distanceSquared(motionPrediction.getVectorState().getPositionDelta()) < d2 + d2 * motionPrediction.getVectorState().getPositionDelta().lengthSquared()) {
               list2.add(motionPrediction);
            }
         }

         return list2;
      } else {
         ArrayList<MotionPrediction> list3 = new ArrayList<>();

         for (MotionPrediction motionPrediction2 : list) {
            if (this.matchesAxisTolerance(vector.getY(), motionPrediction2.getVectorState().getPositionDelta().getY(), d)
               && this.horizontalLengthSquared(vector) - this.horizontalLengthSquared(motionPrediction2.getVectorState().getPositionDelta())
                  < d2 + d2 * this.horizontalLengthSquared(motionPrediction2.getVectorState().getPositionDelta())) {
               list3.add(motionPrediction2);
            }
         }

         return list3;
      }
   }

   private double horizontalDistanceSquared(Vector vector, Vector vector2) {
      return (vector.getX() - vector2.getX()) * (vector.getX() - vector2.getX()) + (vector.getZ() - vector2.getZ()) * (vector.getZ() - vector2.getZ());
   }

   private double horizontalLengthSquared(Vector vector) {
      return vector.getX() * vector.getX() + vector.getZ() * vector.getZ();
   }

   private double horizontalLength(Vector vector) {
      return Math.sqrt(this.horizontalLengthSquared(vector));
   }

   private boolean matchesAxisTolerance(double d, double d2, double d3) {
      double d4 = Math.abs(d - d2);
      double d5 = Math.abs(d2);
      return (d4 - d3) / d5 < d3;
   }

   public static MovementResolution evaluateMovement(Move move, MovementContext movementContext) {
      return move.getSetbackRequest(movementContext);
   }
}
