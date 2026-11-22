package me.konsolas.aac.checks;


import net.jodah.expiringmap.ExpiringMap;
import me.konsolas.aac.AAC;
import me.konsolas.aac.checks.movement.MovementTransition;
import me.konsolas.aac.compat.CollisionBoxAccess;
import me.konsolas.aac.compat.ProtocolVersion;
import me.konsolas.aac.movement.EntityDimensions;
import me.konsolas.aac.movement.MovementResolution;
import me.konsolas.aac.movement.PlayerMovementState;
import me.konsolas.aac.movement.VelocityExtrapolator;
import me.konsolas.aac.punishment.ViolationLogger;
import me.konsolas.aac.util.BoundingBox;
import me.konsolas.aac.util.CollisionUtils;
import me.konsolas.aac.util.GroundCollisionState;
import me.konsolas.aac.util.RelativeBlockAccess;
import me.konsolas.aac.util.ToleranceChecks;
import me.konsolas.aac.util.VectorFormatter;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.PlayerViolationEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public final class CheckManager implements Listener {
   private final AAC plugin;
   private final Map<HackType, Check> enabledChecks = new EnumMap<>(HackType.class);
   private final Map<Player, PlayerData> playerDataMap = Collections.synchronizedMap(new WeakHashMap<>());
   private final Cache<BoundingBox, Vector> pistonSlimeCache;
   private final Cache<BoundingBox, Vector> pistonBlockCache;
   private final Set<UUID> relogCooldownPlayers;
   private PacketAdapter packetListener;
   private RiptideListener riptideListener;
   private int tickTaskId;
   private final AtomicLong tickCounter = new AtomicLong(0L);
   private final Vector moveViolationSlowdown;
   private final double maxClipAdjustSquared;
   private final boolean revertBlocks;

   public CheckManager(AAC plugin) {
      this.plugin = plugin;
      double d = plugin.getConfig().getDouble("move_vl_slowdown");
      this.moveViolationSlowdown = new Vector(d, 1.0, d);
      double d2 = plugin.getConfig().getDouble("max_clip_adjust");
      this.maxClipAdjustSquared = d2 * d2;
      this.revertBlocks = plugin.getConfig().getBoolean("revert_blocks");
      long relogProtection = plugin.getConfig().getLong("relog_protection");
      this.relogCooldownPlayers = Collections.newSetFromMap(ExpiringMap.builder().expiration(relogProtection, TimeUnit.MILLISECONDS).build());
      long ticks = plugin.getConfig().getLong("piston_wait_time");
      this.pistonSlimeCache = CacheBuilder.newBuilder().expireAfterWrite(ticks, TimeUnit.MILLISECONDS).build();
      this.pistonBlockCache = CacheBuilder.newBuilder().expireAfterWrite(ticks, TimeUnit.MILLISECONDS).build();
   }

   public void start() {
      PluginManager pluginManager = this.plugin.getServer().getPluginManager();
      pluginManager.registerEvents(this, this.plugin);
      if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)) {
         this.riptideListener = new RiptideListener(this);
         pluginManager.registerEvents(this.riptideListener, this.plugin);
      }

      BukkitScheduler bukkitScheduler = this.plugin.getServer().getScheduler();
      this.tickTaskId = bukkitScheduler.scheduleSyncRepeatingTask(this.plugin, this::tick, 1L, 1L);
   }

   private void tick() {
      long ticks = this.tickCounter.incrementAndGet();

      for (Check check : this.enabledChecks.values()) {
         check.tick(ticks);
      }
   }

   public void shutdown() {
      this.getEnabledCheckTypes().forEach(this::disableCheck);
      Object obj = null;
      CheckManager checkManager = this;
      HandlerList.unregisterAll(checkManager);
      if (this.riptideListener != null) {
         RiptideListener riptideListener = this.riptideListener;
         HandlerList.unregisterAll(riptideListener);
      }

      if (this.packetListener != null) {
         obj = ProtocolLibrary.getProtocolManager();
         PacketAdapter packetAdapter = this.packetListener;
         ProtocolManager protocolManager = (ProtocolManager)obj;
         protocolManager.removePacketListener(packetAdapter);
      }

      Server server = this.plugin.getServer();
      obj = server.getScheduler();
      int value = this.tickTaskId;
      BukkitScheduler bukkitScheduler = (BukkitScheduler)obj;
      bukkitScheduler.cancelTask(value);
   }

   private PlayerData createPlayerData(Player player) {
      PlayerData playerData = new PlayerData(player);

      for (Check check : this.enabledChecks.values()) {
         playerData.checkData.put(check.getHackType(), check.createPlayerData());
      }

      return playerData;
   }

   public void enableCheck(HackType hackType) {
      if (this.enabledChecks.remove(hackType) != null) {
         this.plugin.getLogger().warning("Tried to enable enabled check " + hackType);
      }

      Check check;
      try {
         this.enabledChecks
            .put(
               hackType,
               check = (Check)Class.forName("me.konsolas.aac.checks." + hackType.toString())
                  .getConstructor(this.getClass())
                  .newInstance(this)
            );
      } catch (ReflectiveOperationException ex) {
         this.plugin.getLogger().severe("Failed to enable " + hackType + ". Stack trace follows");
         ex.printStackTrace();
         return;
      }

      for (Player player : Bukkit.getOnlinePlayers()) {
         PlayerData playerData = (PlayerData)this.playerDataMap.get(player);
         if (playerData != null) {
            playerData.checkData.put(hackType, check.createPlayerData());
         }
      }

      this.reregisterPacketListener();
      ViolationLogger violationLogger = this.plugin.getViolationLogger();
      String str = hackType + ": enabled";
      violationLogger.logSystem(str);
   }

   public void disableCheck(HackType hackType) {
      ViolationLogger violationLogger = null;
      Check check = (Check)this.enabledChecks.remove(hackType);
      if (check != null) {
         this.reregisterPacketListener();

         for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = (PlayerData)this.playerDataMap.get(player);
            if (playerData != null) {
               playerData.checkData.remove(hackType);
            }
         }

         violationLogger = this.plugin.getViolationLogger();
         String str = hackType + ": disabled";
         violationLogger.logSystem(str);
      } else {
         this.plugin.getLogger().warning("Tried to disable disabled check" + hackType);
      }
   }

   public Optional<Check> getCheck(HackType hackType) {
      return Optional.ofNullable(this.enabledChecks.get(hackType));
   }

   public Collection<HackType> getEnabledCheckTypes() {
      return this.enabledChecks.keySet();
   }

   public boolean isCheckEnabled(HackType hackType) {
      return this.enabledChecks.containsKey(hackType);
   }

   public AAC getPlugin() {
      return this.plugin;
   }

   public long getCurrentTick() {
      return this.tickCounter.get();
   }

   private void reregisterPacketListener() {
      HashSet<PacketType> set = new HashSet<>();
      set.add(Client.BLOCK_DIG);
      set.add(com.comphenix.protocol.PacketType.Play.Server.ENTITY_VELOCITY);

      for (Check check : this.enabledChecks.values()) {
         set.addAll(check.getDependencies());
      }

      if (this.packetListener != null) {
         ProtocolLibrary.getProtocolManager().removePacketListener(this.packetListener);
      }

      if (!set.isEmpty()) {
         this.packetListener = new CheckPacketListener(this, set);
         ProtocolLibrary.getProtocolManager().addPacketListener(this.packetListener);
      }
   }

   public Cache<BoundingBox, Vector> getPistonSlimeCache() {
      return this.pistonSlimeCache;
   }

   public Cache<BoundingBox, Vector> getPistonBlockCache() {
      return this.pistonBlockCache;
   }

   public Optional<PlayerData> getPlayerData(Player player) {
      if (!player.getClass().equals(ProtocolVersion.CRAFT_PLAYER_CLASS)) {
         return Optional.empty();
      }

      if (!player.hasPermission("AAC.bypass")
         && (player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SURVIVAL)
         && !player.getAllowFlight()
         && !player.isSleeping()
         && !player.isDead()) {
         return Optional.of(this.playerDataMap.computeIfAbsent(player, this::createPlayerData));
      }

      this.playerDataMap.remove(player);
      return Optional.empty();
   }

   public boolean isPlayerSafeForCombatCheck(Player player) {
      Player player2 = player;
      Optional optional = this.getPlayerData(player2);
      if (optional.isPresent()) {
         PlayerMovementState playerMovementState = ((PlayerData)optional.get()).getMovementState();
         if (playerMovementState.lastDigPosition != null) {
            Location location = playerMovementState.lastDigPosition.toLocation(player.getWorld());
            if (player.getLocation().distanceSquared(location) > 49.0) {
               playerMovementState.lastDigPosition = null;
            }

            double d = new BoundingBox(location).rayTraceDistance(player.getEyeLocation().toVector(), player.getLocation().getDirection());
            if (d < 6.0) {
               return false;
            }
         }

         return !playerMovementState.blockingOrDigging && playerMovementState.lastDigTick + 5L < this.tickCounter.get() && playerMovementState.lastBlockPlaceTick + 2L < this.tickCounter.get();
      } else {
         return false;
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
      if (this.relogCooldownPlayers.contains(event.getUniqueId())) {
         event.disallow(Result.KICK_OTHER, Objects.requireNonNull(this.plugin.getConfig().getString("relog_message", "")));
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      Optional optional = this.getPlayerData(player);
      if (optional.isPresent()) {
         this.getPlugin().getLogger().info(((PlayerData)optional.get()).getClientVersion() + " detected for " + event.getPlayer().getName());

         for (Check check : this.enabledChecks.values()) {
            check.onJoin(event.getPlayer(), (PlayerData)optional.get());
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.relogCooldownPlayers.remove(event.getPlayer().getUniqueId());
      this.relogCooldownPlayers.add(event.getPlayer().getUniqueId());
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onPlayerKick(PlayerKickEvent event) {
      this.relogCooldownPlayers.remove(event.getPlayer().getUniqueId());
      this.relogCooldownPlayers.add(event.getPlayer().getUniqueId());
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onPlayerMove(PlayerMoveEvent event) {
      if (event.getFrom().getWorld().equals(event.getTo().getWorld())) {
         Player player = event.getPlayer();
         Optional optional = this.getPlayerData(player);
         if (optional.isPresent()) {
            RelativeBlockAccess relativeBlockAccess = new RelativeBlockAccess(event.getFrom());
            RelativeBlockAccess relativeBlockAccess2 = new RelativeBlockAccess(event.getTo());
            boolean flag = ((PlayerData)optional.get()).getMovementState().inVehicle;
            ((PlayerData)optional.get()).getMovementState().inVehicle = event.getPlayer().getVehicle() != null;
            boolean flag2 = false;
            if (flag != ((PlayerData)optional.get()).getMovementState().inVehicle && event.getTo().distanceSquared(event.getFrom()) <= 16.0) {
               flag2 = true;
            }

            GroundCollisionState groundCollisionState;
            GroundCollisionState groundCollisionState2;
            if (event.getTo().distanceSquared(event.getFrom()) < 100.0) {
               groundCollisionState = ((PlayerData)optional.get()).getMovementState().lastGroundState;
               groundCollisionState2 = CollisionBoxAccess.checkMoveGroundCollision(event, groundCollisionState);
            } else {
               groundCollisionState = ((PlayerData)optional.get()).getMovementState().lastGroundState;
               groundCollisionState2 = GroundCollisionState.FALSE_NORMAL;
            }

            if (((PlayerData)optional.get()).getMovementState().pendingKnockback != null) {
               if (System.currentTimeMillis() - ((PlayerData)optional.get()).getMovementState().lastKnockbackMillis <= 2500L) {
                  Vector vector = ((PlayerData)optional.get()).getMovementState().pendingKnockback.clone();
                  Player player2 = event.getPlayer();
                  Location location = event.getFrom();
                  Vector vector2 = vector;
                  Location location2 = location;
                  Player player3 = player2;
                  CollisionUtils.getCollidingBoxes(player3, location2, vector2);
                  if (vector.length() == 0.0) {
                     ((PlayerData)optional.get()).getMovementState().pendingKnockback = null;
                  } else {
                     Vector vector3 = event.getTo().toVector().subtract(event.getFrom().toVector());
                     if (ToleranceChecks.isWithinTolerance(vector3.getX(), vector.getX(), 0.28284271247)
                        && ToleranceChecks.isWithinTolerance(vector3.getY(), vector.getY(), 0.01)
                        && ToleranceChecks.isWithinTolerance(vector3.getZ(), vector.getZ(), 0.28284271247)
                        && --((PlayerData)optional.get()).getMovementState().knockbackTicksRemaining <= 0) {
                        ((PlayerData)optional.get()).getMovementState().pendingKnockback = null;
                     }
                  }
               } else {
                  ((PlayerData)optional.get()).getMovementState().pendingKnockback = null;
               }
            }

            MovementTransition movementTransition = new MovementTransition(event.getFrom(), event.getTo(), relativeBlockAccess, relativeBlockAccess2, groundCollisionState, groundCollisionState2);
            MovementResolution movementResolution = null;
            Check check = null;

            for (Check check2 : this.enabledChecks.values()) {
               if ((movementResolution = check2.move(event.getPlayer(), (PlayerData)optional.get(), movementTransition)) != null) {
                  check = check2;
                  break;
               }
            }

            if (movementResolution != null) {
               Location location3 = movementResolution.getLocation() == null ? event.getFrom() : movementResolution.getLocation();
               if (location3.getWorld() != event.getFrom().getWorld()) {
                  this.getPlugin().getLogger().warning("Setback location in a different world requested");
                  location3 = event.getFrom();
               }

               Vector vector4;
               String str;
               if (movementResolution.getMovement() != null) {
                  vector4 = movementResolution.getMovement();
                  str = "setbackMotion";
               } else {
                  vector4 = ((PlayerData)optional.get()).getMovementState().pendingKnockback;
                  if (vector4 == null) {
                     vector4 = ((PlayerData)optional.get()).getMovementState().lastVelocity;
                     str = "globalMotion";
                  } else {
                     str = "pendingMotion";
                  }
               }

               Vector vector5 = event.getPlayer().getLocation().toVector();
               Player player4 = event.getPlayer();
               BoundingBox box = new BoundingBox(vector5, EntityDimensions.forEntity(player4));
               Player player5 = event.getPlayer();
               BoundingBox box2 = box.expand(0.001, 0.001, 0.001);
               Player player6 = player5;
               List list = CollisionBoxAccess.getCubes(player6, box2);
               BoundingBox box3 = box.resolveCollision(list);
               if (box3.getBottomCenter().distanceSquared(location3.toVector()) < this.maxClipAdjustSquared) {
                  location3 = box3.getBottomCenter().toLocation(Objects.requireNonNull(location3.getWorld()), location3.getYaw(), location3.getPitch());
               }

               Location location4 = VelocityExtrapolator.predictLocation(
                  event.getPlayer(), vector4, ((PlayerData)optional.get()).getMovementState().lastGroundState.isOnGround(), location3, movementResolution.getScale()
               );
               float f = event.getPlayer().getFallDistance();
               event.getPlayer().teleport(location4, TeleportCause.UNKNOWN);
               event.getPlayer().setFallDistance(f + (float)(event.getFrom().getY() - location4.getY()));
               PlayerMoveEvent event2 = new PlayerMoveEvent(event.getPlayer(), event.getFrom(), location4);
               groundCollisionState2 = CollisionBoxAccess.checkMoveGroundCollision(event2, groundCollisionState);
               Vector vector6 = VelocityExtrapolator.predictVelocity(vector4, movementResolution.getScale() + 1.0);
               if (groundCollisionState2.isOnGround()) {
                  vector6.setY(0);
               }

               vector6.multiply(this.moveViolationSlowdown);
               if (vector6.lengthSquared() >= 9.0) {
                  vector6.normalize().multiply(3.0);
               }

               event.getPlayer().setVelocity(vector6);
               ViolationLogger violationLogger = this.getPlugin().getViolationLogger();
               Player player7 = event.getPlayer();
               StringBuilder sb = new StringBuilder().append(check.getHackType()).append(": setback from ");
               Vector vector7 = event.getFrom().toVector();
               StringBuilder sb2 = sb.append(VectorFormatter.formatVector(vector7)).append(": ");
               Vector vector8 = location3.toVector();
               StringBuilder sb3 = sb2.append(VectorFormatter.formatVector(vector8)).append("->");
               Vector vector9 = location4.toVector();
               StringBuilder sb4 = sb3.append(VectorFormatter.formatVector(vector9))
                  .append(" fallD: ")
                  .append(f)
                  .append(" ground: ")
                  .append(groundCollisionState2)
                  .append(" motion: ");
               Vector vector10 = vector6;
               String str2 = sb4.append(VectorFormatter.formatVector(vector10)).append(" ").append(str).toString();
               Player player8 = player7;
               violationLogger.logViolation(player8, str2);
               ((PlayerData)optional.get()).getMovementState().lastSetbackTick = this.tickCounter.get();
            } else if (flag2) {
               ((PlayerData)optional.get()).getMovementState().lastVelocity = new Vector(0.0, -0.078, 0.0);
            } else {
               ((PlayerData)optional.get()).getMovementState().lastVelocity = event.getTo().toVector().subtract(event.getFrom().toVector());
            }

            ((PlayerData)optional.get()).getMovementState().lastGroundState = groundCollisionState2;
            if (this.tickCounter.get() > ((PlayerData)optional.get()).getMovementState().lastBlockPlaceTick + 1L) {
               ((PlayerData)optional.get()).getMovementState().revertedBlock = null;
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
      if (event.getPlayer().hasPermission("AAC.notify")) {
         String str = Objects.requireNonNull(this.plugin.getConfig().getString("staff_chat_prefix", ""));
         if (str.length() > 0 && event.getMessage().startsWith(str)) {
            event.setCancelled(true);
            AAC.runOnMainThread(() -> event.getPlayer().performCommand("aac notify " + event.getMessage().substring(str.length())));
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onVehicleEnter(VehicleEnterEvent event) {
      if (event.getEntered() instanceof Player) {
         Player player = (Player)event.getEntered();
         Optional optional = this.getPlayerData(player);
         if (optional.isPresent()) {
            ((PlayerData)optional.get()).getMovementState().pendingKnockback = null;
            ((PlayerData)optional.get()).getMovementState().lastVelocity = new Vector(0, 0, 0);
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onVehicleExit(VehicleExitEvent event) {
      if (event.getExited() instanceof Player) {
         Player player = (Player)event.getExited();
         Optional optional = this.getPlayerData(player);
         if (optional.isPresent()) {
            ((PlayerData)optional.get()).getMovementState().pendingKnockback = null;
            ((PlayerData)optional.get()).getMovementState().lastVelocity = new Vector(0, 0, 0);
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      Player player = event.getPlayer();
      Optional optional = this.getPlayerData(player);
      if (optional.isPresent()) {
         ((PlayerData)optional.get()).getMovementState().pendingKnockback = null;
         ((PlayerData)optional.get()).getMovementState().lastVelocity = new Vector(0, 0, 0);

         for (Check check : this.enabledChecks.values()) {
            check.onTeleport(event.getPlayer(), (PlayerData)optional.get(), event.getTo());
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      Optional optional = this.getPlayerData(player);
      if (optional.isPresent()) {
         for (Check check : this.enabledChecks.values()) {
            check.onPlayerInteract(event.getPlayer(), (PlayerData)optional.get(), event);
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
      Player player = event.getPlayer();
      Optional optional = this.getPlayerData(player);
      if (optional.isPresent()) {
         for (Check check : this.enabledChecks.values()) {
            check.onPlayerItemConsume(event.getPlayer(), (PlayerData)optional.get(), event);
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onBlockBreak(BlockBreakEvent event) {
      if (event.getClass().equals(BlockBreakEvent.class)) {
         Player player = event.getPlayer();
         Optional optional = this.getPlayerData(player);
         if (optional.isPresent()) {
            for (Check check : this.enabledChecks.values()) {
               check.onBlockBreak(event.getPlayer(), (PlayerData)optional.get(), event);
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onBlockPlace(BlockPlaceEvent event) {
      if (event.getClass().equals(BlockPlaceEvent.class)) {
         Player player = event.getPlayer();
         Optional optional = this.getPlayerData(player);
         if (optional.isPresent()) {
            ((PlayerData)optional.get()).getMovementState().lastBlockPlaceTick = this.tickCounter.get();
            if (this.revertBlocks) {
               ((PlayerData)optional.get()).getMovementState().revertedBlock = event.getBlock();
               if (Math.abs(((PlayerData)optional.get()).getMovementState().lastSetbackTick - ((PlayerData)optional.get()).getMovementState().lastBlockPlaceTick) <= 1L) {
                  ViolationLogger violationLogger = this.getPlugin().getViolationLogger();
                  String str = "revert_blocks: cancelled placement of "
                     + event.getBlockPlaced().getType()
                     + " at "
                     + event.getBlockPlaced().getLocation().toVector();
                  Player player2 = event.getPlayer();
                  violationLogger.logViolation(player2, str);
                  event.setCancelled(true);
               }
            }

            for (Check check : this.enabledChecks.values()) {
               check.onBlockPlace(event.getPlayer(), (PlayerData)optional.get(), event);
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onBlockDamage(BlockDamageEvent event) {
      if (event.getClass().equals(BlockDamageEvent.class)) {
         Player player = event.getPlayer();
         Optional optional = this.getPlayerData(player);
         if (optional.isPresent()) {
            for (Check check : this.enabledChecks.values()) {
               check.onBlockDamage(event.getPlayer(), (PlayerData)optional.get(), event);
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onInventoryOpen(InventoryOpenEvent event) {
      if (event.getPlayer() instanceof Player) {
         Player player = (Player)event.getPlayer();
         Optional optional = this.getPlayerData(player);
         if (optional.isPresent()) {
            for (Check check : this.enabledChecks.values()) {
               check.onInventoryOpen((Player)event.getPlayer(), (PlayerData)optional.get(), event);
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getClass().equals(EntityDamageByEntityEvent.class)) {
         if (event.getDamager() instanceof Player && event.getCause() == DamageCause.ENTITY_ATTACK) {
            Player player = (Player)event.getDamager();
            Player player2 = player;
            Optional optional = this.getPlayerData(player2);
            if (optional.isPresent()) {
               for (Check check : this.enabledChecks.values()) {
                  check.attack(player, (PlayerData)optional.get(), event);
               }
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onEntitySpawn(EntitySpawnEvent event) {

      for (Check check : this.enabledChecks.values()) {
         check.onEntitySpawn(event.getEntity());
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onEntityShootBow(EntityShootBowEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         Player player2 = player;
         Optional optional = this.getPlayerData(player2);
         if (optional.isPresent()) {
            for (Check check : this.enabledChecks.values()) {
               check.onEntityShootBow(player, (PlayerData)optional.get(), event);
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onEntityRegainHealth(EntityRegainHealthEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         Player player2 = player;
         Optional optional = this.getPlayerData(player2);
         if (optional.isPresent()) {
            for (Check check : this.enabledChecks.values()) {
               check.onEntityRegainHealth(player, (PlayerData)optional.get(), event);
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onPlayerViolation(PlayerViolationEvent event) {
      Player player = event.getPlayer();
      Optional optional = this.getPlayerData(player);
      if (optional.isPresent()) {
         for (Check check : this.enabledChecks.values()) {
            check.onViolation(event.getPlayer(), (PlayerData)optional.get(), event.getHackType(), event.getViolations());
         }
      }
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onBlockPistonExtend(BlockPistonExtendEvent event) {
      BlockFace face = event.getDirection();
      Vector vector = new Vector(face.getModX(), face.getModY(), face.getModZ());

      for (Block block : event.getBlocks()) {
         BoundingBox box = new BoundingBox(block.getLocation()).translate(vector);
         if (block.getType() == Material.SLIME_BLOCK) {
            this.pistonBlockCache.put(box, vector);
         } else {
            this.pistonSlimeCache.put(box, vector);
         }
      }

      this.pistonSlimeCache.put(new BoundingBox(event.getBlock().getLocation()).translate(vector), vector);
   }

   public static Map<HackType, Check> getEnabledChecksMap(CheckManager checkManager) {
      return checkManager.enabledChecks;
   }

   public static AAC getPluginInstance(CheckManager checkManager) {
      return checkManager.plugin;
   }

   public static AtomicLong getTickCounter(CheckManager checkManager) {
      return checkManager.tickCounter;
   }
}
