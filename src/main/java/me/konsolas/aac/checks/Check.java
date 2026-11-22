package me.konsolas.aac.checks;


import me.konsolas.aac.checks.movement.MovementTransition;
import me.konsolas.aac.compat.PlayerPingAccess;
import me.konsolas.aac.movement.MovementResolution;
import me.konsolas.aac.punishment.ViolationDecaySettings;
import me.konsolas.aac.punishment.ViolationLogger;
import me.konsolas.aac.util.TickRateMonitor;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.PlayerViolationEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.util.Vector;

public abstract class Check {
   protected final CheckManager manager;
   private final HackType hackType;
   private final Supplier<CheckPlayerData> dataFactory;
   private final ViolationDecaySettings decaySettings;
   private final BiPredicate<Player, CheckPlayerData> bypassPredicate;
   private final Collection<PacketType> dependencies;
   private final boolean mitigationEnabled;
   private final int minimumMitigationViolationLevel;
   private final Map<String, Object> configurationCache;

   public Check(
      CheckManager checkManager,
      HackType hackType,
      Supplier<CheckPlayerData> supplier,
      Collection<PacketType> collection,
      boolean flag,
      BiPredicate<Player, CheckPlayerData> biPredicate
   ) {
      super();
      this.configurationCache = new HashMap<>();
      this.manager = checkManager;
      this.hackType = hackType;
      this.dataFactory = supplier;
      this.dependencies = collection != null ? Collections.unmodifiableCollection(collection) : Collections.emptySet();
      String str = "decay.interval";
      long ticks = this.getLongOption(str);
      String str2 = "decay.delay";
      long ticks2 = this.getLongOption(str2);
      String str3 = "decay.amount";
      this.decaySettings = new ViolationDecaySettings(ticks, ticks2, this.getIntOption(str3));
      this.bypassPredicate = biPredicate;
      if (flag) {
         String str4 = "mitigation.enabled";
         this.mitigationEnabled = this.getBooleanOption(str4);
         String str5 = "mitigation.min_vl";
         this.minimumMitigationViolationLevel = this.getIntOption(str5);
      } else {
         this.mitigationEnabled = false;
         this.minimumMitigationViolationLevel = 0;
      }
   }

   protected boolean shouldMitigate(Player player) {
      if (this.mitigationEnabled) {
         Player player2 = player;
         if (this.manager.getPlayerData(player2).isPresent()) {
            Player player3 = player;
            if (((PlayerData)this.manager.getPlayerData(player3).get()).getCheckData(this.hackType).violationLevel > this.minimumMitigationViolationLevel) {
               return true;
            }
         }
      }

      return false;
   }

   protected double getDoubleOption(String option) {
      if (this.configurationCache.containsKey(option)) {
         return (Double)this.configurationCache.get(option);
      } else {
         String str = option;
         Object obj = this.getOption(str);
         if (obj instanceof Number) {
            Number number = (Number)obj;
            this.configurationCache.put(option, number.doubleValue());
            return number.doubleValue();
         } else {
            throw new IllegalArgumentException("Config option " + option + " of " + this.hackType + " must be a floating-point number");
         }
      }
   }

   protected long getLongOption(String option) {
      if (this.configurationCache.containsKey(option)) {
         return (Long)this.configurationCache.get(option);
      } else {
         String str = option;
         Object obj = this.getOption(str);
         if (obj instanceof Number) {
            Number number = (Number)obj;
            this.configurationCache.put(option, number.longValue());
            return number.longValue();
         } else {
            throw new IllegalArgumentException("Config option " + option + " of " + this.hackType + " must be a long integer");
         }
      }
   }

   protected int getIntOption(String option) {
      if (this.configurationCache.containsKey(option)) {
         return (Integer)this.configurationCache.get(option);
      } else {
         String str = option;
         Object obj = this.getOption(str);
         if (obj instanceof Number) {
            Number number = (Number)obj;
            this.configurationCache.put(option, number.intValue());
            return number.intValue();
         } else {
            throw new IllegalArgumentException("Config option " + option + " of " + this.hackType + " must be an integer");
         }
      }
   }

   protected boolean getBooleanOption(String option) {
      if (this.configurationCache.containsKey(option)) {
         return (Boolean)this.configurationCache.get(option);
      } else {
         String str = option;
         Object obj = this.getOption(str);
         if (obj instanceof Boolean) {
            Boolean boolValue = (Boolean)obj;
            this.configurationCache.put(option, boolValue);
            return boolValue;
         } else {
            throw new IllegalArgumentException("Config option " + option + " of " + this.hackType + " must be true or false");
         }
      }
   }

   @SuppressWarnings("unchecked")
   protected List<String> getStringListOption(String option) {
      if (this.configurationCache.containsKey(option)) {
         return (List<String>)this.configurationCache.get(option);
      }

      String str = option;
      Object obj = this.getOption(str);
      if (!(obj instanceof List) || !((List<?>)obj).isEmpty() && !(((List<?>)obj).get(0) instanceof String)) {
         throw new IllegalArgumentException("Config option " + option + " of " + this.hackType + " must be a list of strings");
      }

      List<String> list = (List<String>)obj;
      this.configurationCache.put(option, list);
      return list;
   }

   protected Object getOption(String option) {
      if (this.configurationCache.containsKey(option)) {
         return this.configurationCache.get(option);
      } else {
         String str = this.hackType.name().toLowerCase() + "." + option;
         Object obj = this.manager.getPlugin().getConfig().get(str);
         if (obj == null) {
            this.manager.getPlugin().getLogger().severe("Missing configuration option " + option + " for check " + this.hackType);
            throw new IllegalArgumentException("Option " + str + " not found");
         } else {
            this.configurationCache.put(option, obj);
            return obj;
         }
      }
   }

   public HackType getHackType() {
      return this.hackType;
   }

   public CheckPlayerData createPlayerData() {
      return this.dataFactory.get();
   }

   public Collection<PacketType> getDependencies() {
      return this.dependencies;
   }

   protected boolean addViolation(Player player, int violations, String reason) {
      Optional<PlayerData> optional = this.manager.getPlayerData(player);
      if (optional.isPresent()) {
         PlayerViolationEvent event = new PlayerViolationEvent(player, this.hackType, violations, reason);
         Bukkit.getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            PlayerData playerData = optional.get();
            CheckPlayerData checkData = playerData.getCheckData(this.hackType);
            checkData.violationLevel = checkData.violationLevel + event.getViolations();
            playerData.getCheckData(this.hackType).lastViolationMillis = this.manager.getCurrentTick();
            if (event.getViolations() != violations) {
               ViolationLogger violationLogger = this.manager.getPlugin().getViolationLogger();
               String str = this.hackType + ": modified VL:" + violations + "->" + event.getViolations();
               Player player2 = player;
               violationLogger.logViolation(player2, str);
            }

            StringBuilder sb = new StringBuilder().append(this.hackType).append(": ").append(reason).append(" [ping:");
            Player player3 = player;
            String str2 = sb.append(PlayerPingAccess.getPing(player3))
               .append(" tps:")
               .append(Math.round(TickRateMonitor.getTicksPerSecond() * 100.0) / 100.0)
               .append("] VL:")
               .append(playerData.getCheckData(this.hackType).violationLevel)
               .toString();
            ViolationLogger violationLogger2 = this.manager.getPlugin().getViolationLogger();
            String str3 = str2;
            Player player4 = player;
            violationLogger2.logViolation(player4, str3);
            Player player5 = player;
            return this.shouldMitigate(player5);
         } else {
            ViolationLogger violationLogger3 = this.manager.getPlugin().getViolationLogger();
            String str4 = this.hackType + ": cancelled VL:" + violations;
            Player player6 = player;
            violationLogger3.logViolation(player6, str4);
            return false;
         }
      } else {
         return false;
      }
   }

   private void updateViolationLevel(Player player, int i) {
      PlayerData playerData = this.manager.getPlayerData(player)
         .orElseThrow(() -> new IllegalStateException("Subtracted violations from a player without data"));
      playerData.getCheckData(this.hackType).violationLevel -= i;
      if (playerData.getCheckData(this.hackType).violationLevel < 0) {
         playerData.getCheckData(this.hackType).violationLevel = 0;
      }
   }

   protected Optional<PlayerData> getPlayerData(Player player) {
      return this.manager.getPlayerData(player);
   }

   protected void forAllPlayers(BiConsumer<Player, CheckPlayerData> biConsumer) {
      for (Player player : Bukkit.getOnlinePlayers()) {
         this.manager.getPlayerData(player).ifPresent(playerData -> biConsumer.accept(player, playerData.getCheckData(this.hackType)));
      }
   }

   public void tick(long currentTick) {
      if (currentTick % ViolationDecaySettings.getInterval(this.decaySettings) == 0L) {
         this.forAllPlayers((player, checkData) -> {
            if (currentTick - ViolationDecaySettings.getDelay(this.decaySettings) >= checkData.lastViolationMillis && this.bypassPredicate.test(player, checkData)) {
               this.updateViolationLevel(player, ViolationDecaySettings.getAmount(this.decaySettings));
            }
         });
      }
   }

   public void onPacket(Player player, PlayerData playerData, PacketEvent event) {
   }

   public void onJoin(Player player, PlayerData playerData) {
   }

   public MovementResolution move(Player player, PlayerData playerData, MovementTransition movementTransition) {
      return null;
   }

   public void onTeleport(Player player, PlayerData playerData, Location location) {
   }

   public void onVelocity(Player player, PlayerData playerData, Vector vector) {
   }

   public void onPlayerInteract(Player player, PlayerData playerData, PlayerInteractEvent event) {
   }

   public void onPlayerItemConsume(Player player, PlayerData playerData, PlayerItemConsumeEvent event) {
   }

   public void onBlockBreak(Player player, PlayerData playerData, BlockBreakEvent event) {
   }

   public void onBlockPlace(Player player, PlayerData playerData, BlockPlaceEvent event) {
   }

   public void onBlockDamage(Player player, PlayerData playerData, BlockDamageEvent event) {
   }

   public void onInventoryOpen(Player player, PlayerData playerData, InventoryOpenEvent event) {
   }

   public void attack(Player player, PlayerData playerData, EntityDamageByEntityEvent event) {
   }

   public void onEntityShootBow(Player player, PlayerData playerData, EntityShootBowEvent event) {
   }

   public void onEntityRegainHealth(Player player, PlayerData playerData, EntityRegainHealthEvent event) {
   }

   public void onEntitySpawn(Entity entity) {
   }

   public void onRiptide(Player player, PlayerData playerData, int i) {
   }

   public void onViolation(Player player, PlayerData playerData, HackType hackType, int i) {
   }
}
