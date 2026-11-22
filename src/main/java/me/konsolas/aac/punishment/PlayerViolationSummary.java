package me.konsolas.aac.punishment;

import java.util.List;
import org.bukkit.entity.Player;

public class PlayerViolationSummary {
   private final Player player;
   private final int totalViolationLevel;
   private final List breakdown;

   private PlayerViolationSummary(Player player, int i, List list) {
      this.player = player;
      this.totalViolationLevel = i;
      this.breakdown = list;
   }

   public Player getPlayer() {
      return this.player;
   }

   public int getTotalViolationLevel() {
      return this.totalViolationLevel;
   }

   public List getBreakdown() {
      return this.breakdown;
   }

   public PlayerViolationSummary(Player player, int i, List list, DefaultViolationWeights defaultViolationWeights) {
      this(player, i, list);
   }
}
