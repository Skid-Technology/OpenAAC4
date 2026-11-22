package me.konsolas.aac.checks.movement;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.movement.MovementResolution;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketEvent;
import java.util.Arrays;
import java.util.Locale;
import me.konsolas.aac.api.HackType;
import org.bukkit.entity.Player;

public class Timer extends Check {
   public Timer(CheckManager checkManager) {
      super(checkManager, HackType.TIMER, TimerPlayerData::new, Arrays.asList(Client.POSITION, Client.POSITION_LOOK, Client.LOOK, Server.POSITION), true, (player, data) -> true);
   }

   @Override
   public void onPacket(Player player, PlayerData playerData, PacketEvent event) {
      TimerPlayerData data = (TimerPlayerData)playerData.getCheckData(HackType.TIMER);
      if (event.getPacketType() == Server.POSITION) {
         synchronized (TimerPlayerData.getBalanceLock(data)) {
            String str = "min_balance";
            double d = this.getDoubleOption(str);
            data.balance--;
            data.balance = Math.max(data.balance, d);
         }
      } else {
         this.addBalance(playerData, 1);
      }
   }

   public void addBalance(PlayerData playerData, int i) {
      TimerPlayerData data = (TimerPlayerData)playerData.getCheckData(HackType.TIMER);
      long now = System.nanoTime();
      synchronized (TimerPlayerData.getBalanceLock(data)) {
         if (data.lastPacketNanos == 0L) {
            data.lastPacketNanos = now - 50000000L;
         }

         long ticks = now - data.lastPacketNanos;
         data.lastPacketNanos = now;
         double d = ticks / 5.0E7;
         String str = "min_balance";
         double d2 = this.getDoubleOption(str);
         data.balance = Math.max(data.balance + i - d, d2);
      }
   }

   @Override
   public MovementResolution move(Player player, PlayerData playerData, MovementTransition movementTransition) {
      TimerPlayerData data = (TimerPlayerData)playerData.getCheckData(HackType.TIMER);
      String str = "max_extra";
      if (data.balance > this.getDoubleOption(str)) {
         data.balance--;
         String str2 = String.format(Locale.UK, "moved too many times: %.2f", data.balance);
         byte b = 1;
         Player player2 = player;
         return this.addViolation(player2, b, str2) ? MovementResolution.setback() : null;
      } else {
         return null;
      }
   }
}
