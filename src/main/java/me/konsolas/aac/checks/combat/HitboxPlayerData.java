package me.konsolas.aac.checks.combat;


import me.konsolas.aac.checks.CheckPlayerData;
import com.google.common.collect.EvictingQueue;
import org.bukkit.util.Vector;

public class HitboxPlayerData extends CheckPlayerData {
   private EvictingQueue<Vector> positionHistory;

   public HitboxPlayerData() {
   }

   public static EvictingQueue<Vector> getPositionHistory(HitboxPlayerData data) {
      return data.positionHistory;
   }

   public static EvictingQueue<Vector> setPositionHistory(HitboxPlayerData data, EvictingQueue<Vector> evictingQueue) {
      return data.positionHistory = evictingQueue;
   }
}
