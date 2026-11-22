package me.konsolas.aac.checks;


import me.konsolas.aac.compat.ProtocolVersion;
import me.konsolas.aac.movement.PlayerMovementState;
import java.util.EnumMap;
import java.util.Map;
import me.konsolas.aac.api.HackType;
import org.bukkit.entity.Player;

public class PlayerData {
   public final Map checkData = new EnumMap<>(HackType.class);
   private final PlayerMovementState movementState = new PlayerMovementState();
   private final ProtocolVersion clientVersion;

   public PlayerData(Player player) {
      Player player2 = player;
      this.clientVersion = ProtocolVersion.fromPlayer(player2);
   }

   public CheckPlayerData getCheckData(HackType hackType) {
      return (CheckPlayerData)this.checkData.get(hackType);
   }

   public PlayerMovementState getMovementState() {
      return this.movementState;
   }

   public ProtocolVersion getClientVersion() {
      return this.clientVersion;
   }

   public static int unusedMarker() {
      return 0;
   }

}
