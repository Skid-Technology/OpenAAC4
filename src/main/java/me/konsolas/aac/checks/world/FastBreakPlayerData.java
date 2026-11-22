package me.konsolas.aac.checks.world;


import me.konsolas.aac.checks.CheckPlayerData;
import org.bukkit.util.Vector;

public class FastBreakPlayerData extends CheckPlayerData {
   public long damageStartMillis;
   public Vector targetBlockPos;
   public long lastBreakMillis;

   public FastBreakPlayerData() {
   }

}
