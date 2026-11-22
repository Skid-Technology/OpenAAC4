package me.konsolas.aac.checks.combat;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.util.PacketMathUtils;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketEvent;
import java.util.Arrays;
import me.konsolas.aac.api.HackType;
import org.bukkit.entity.Player;

public class FightSpeed extends Check {
   public FightSpeed(CheckManager checkManager) {
      super(checkManager, HackType.FIGHTSPEED, FightSpeedPlayerData::new, Arrays.asList(Client.ARM_ANIMATION, Client.USE_ENTITY), false, (player, data) -> true);
   }

   @Override
   public void onPacket(Player player2, PlayerData playerData, PacketEvent event) {
      Killaura.unusedFlag();
      FightSpeedPlayerData data2 = (FightSpeedPlayerData)playerData.getCheckData(HackType.FIGHTSPEED);
      if (PacketMathUtils.isAttackPacket(event.getPacket())) {
         AttackSampleWindow.record(data2.hitWindow);
      } else if (event.getPacketType() == Client.ARM_ANIMATION) {
         Player player3 = player2;
         if (this.manager.isPlayerSafeForCombatCheck(player3)) {
            AttackSampleWindow.record(data2.swingWindow);
         }
      }
   }

   @Override
   public void tick(long currentTick) {
      Killaura.unusedFlag();
      super.tick(currentTick);
      if (currentTick % 20L == 0L) {
         this.forAllPlayers((player, data) -> {
            long now = System.currentTimeMillis();
            FightSpeedPlayerData fsData = (FightSpeedPlayerData)data;
            long ticks = now - fsData.lastCheckMillis;
            fsData.lastCheckMillis = now;
            AttackSampleWindow attackSampleWindow = fsData.hitWindow;
            AttackSampleWindow attackSampleWindow2 = fsData.swingWindow;
            fsData.hitWindow = new AttackSampleWindow();
            fsData.swingWindow = new AttackSampleWindow();
            if (attackSampleWindow != null) {
               double d = AttackSampleWindow.getIntervals(attackSampleWindow).size() * 1000.0 / ticks;
               if (d > this.getIntOption("max_hit")) {
                  int vl = Math.min(AttackSampleWindow.getIntervals(attackSampleWindow).size() - this.getIntOption("max_hit") + 2, 10);
                  this.addViolation(player, vl, "attacked " + AttackSampleWindow.getIntervals(attackSampleWindow).size() + " times in " + ticks + "ms");
               }
            }

            if (attackSampleWindow2 != null) {
               double d2 = AttackSampleWindow.getIntervals(attackSampleWindow2).size() * 1000.0 / ticks;
               if (d2 > this.getIntOption("max_swing")) {
                  int vl = Math.min(AttackSampleWindow.getIntervals(attackSampleWindow2).size() - this.getIntOption("max_hit"), 10);
                  this.addViolation(player, vl, "clicked " + AttackSampleWindow.getIntervals(attackSampleWindow2).size() + " times in " + ticks + "ms");
               }
            }
         });
      }
   }
}
