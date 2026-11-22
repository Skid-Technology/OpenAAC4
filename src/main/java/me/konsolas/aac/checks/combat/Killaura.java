package me.konsolas.aac.checks.combat;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.checks.movement.MovementTransition;
import me.konsolas.aac.movement.MovementResolution;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import me.konsolas.aac.api.HackType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class Killaura extends Check {
   private long currentTick;
   private static final Map<Class<? extends CombatCheckModule>, String> MODULE_NAMES = new CombatModuleNames();
   private final Map<Class<? extends CombatCheckModule>, Integer> enabledModules;

   public Killaura(CheckManager checkManager) {
      super(
         checkManager,
         HackType.KILLAURA,
         KillauraPlayerData::new,
         Arrays.asList(
            Client.ENTITY_ACTION, Client.USE_ENTITY, Client.ARM_ANIMATION, Client.FLYING, Client.POSITION, Client.POSITION_LOOK, Client.LOOK
         ),
         true,
         (player, data) -> true
      );
      this.enabledModules = new HashMap<>();

      for (Class<? extends CombatCheckModule> classVal : MODULE_NAMES.keySet()) {
         String str = "modules." + MODULE_NAMES.get(classVal);
         int value = this.getIntOption(str);
         if (value >= 0) {
            this.enabledModules.put(classVal, value);
         }
      }
   }

   private void initKillauraData(Player player, KillauraPlayerData data) {
      if (KillauraPlayerData.getModules(data) == null) {
         KillauraPlayerData.setModules(data, this.enabledModules.keySet().stream().map(clazz -> {
            Consumer<CombatModuleViolation> violationConsumer = violation -> this.addViolation(
               player,
               this.enabledModules.get(clazz),
               MODULE_NAMES.get(clazz) + ": (" + violation.getTier() + ") " + violation.getDescription()
            );

            try {
               return clazz.getConstructor(Consumer.class, KillauraPlayerData.class).newInstance(violationConsumer, data);
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
               throw new RuntimeException(ex);
            }
         }).collect(Collectors.toSet()));
      }

      if (data.combatReadySupplier == null) {
         data.combatReadySupplier = () -> this.manager.isPlayerSafeForCombatCheck(player);
      }
   }

   @Override
   public void tick(long currentTick) {
      super.tick(currentTick);
      this.currentTick = currentTick;
   }

   @Override
   public void onPacket(Player player, PlayerData playerData, PacketEvent event) {
      KillauraPlayerData data = (KillauraPlayerData)playerData.getCheckData(HackType.KILLAURA);
      this.initKillauraData(player, data);

      for (CombatCheckModule combatCheckModule : KillauraPlayerData.getModules(data)) {
         combatCheckModule.onPacket(event, this.currentTick);
      }
   }

   @Override
   public MovementResolution move(Player player, PlayerData playerData, MovementTransition movementTransition) {
      KillauraPlayerData data = (KillauraPlayerData)playerData.getCheckData(HackType.KILLAURA);
      this.initKillauraData(player, data);
      PlayerMoveEvent event = new PlayerMoveEvent(player, movementTransition.getFrom(), movementTransition.getTo());

      for (CombatCheckModule combatCheckModule : KillauraPlayerData.getModules(data)) {
         combatCheckModule.onPlayerMove(event, this.currentTick);
      }

      return null;
   }

   @Override
   public void attack(Player player, PlayerData playerData, EntityDamageByEntityEvent event) {
      KillauraPlayerData data = (KillauraPlayerData)playerData.getCheckData(HackType.KILLAURA);
      this.initKillauraData(player, data);

      for (CombatCheckModule combatCheckModule : KillauraPlayerData.getModules(data)) {
         combatCheckModule.onEntityDamage(event, this.currentTick);
      }

      KillauraPlayerData.setLastAttackTick(data, this.currentTick);
      Player player2 = player;
      if (this.shouldMitigate(player2)) {
         event.setCancelled(true);
      }
   }

   @Override
   public void onViolation(Player player, PlayerData playerData, HackType hackType, int i) {
      KillauraPlayerData data = (KillauraPlayerData)playerData.getCheckData(HackType.KILLAURA);
      this.initKillauraData(player, data);
      if (KillauraPlayerData.getLastAttackTick(data) + 20L > this.currentTick && hackType == HackType.MOVE && i < 50) {
         String str = "modules.D1";
         int value = this.getIntOption(str);
         if (value >= 0 && KillauraPlayerData.getMoveBuffer(data).record(this.currentTick)) {
            ViolationTier violationTier = KillauraPlayerData.getMoveBuffer(data).getCount() > 20
               ? ViolationTier.HIGH
               : ViolationTier.MEDIUM;
            String str2 = "D1: (" + violationTier + ") u:" + i + " v: " + KillauraPlayerData.getMoveBuffer(data).getCount();
            int value2 = value;
            Player player2 = player;
            this.addViolation(player2, value2, str2);
         }
      }
   }

   public static boolean unusedFlag() {
      return false;
   }
}
