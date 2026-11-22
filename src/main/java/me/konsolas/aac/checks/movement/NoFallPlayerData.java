package me.konsolas.aac.checks.movement;


import me.konsolas.aac.checks.CheckPlayerData;
public class NoFallPlayerData extends CheckPlayerData {
   public boolean packetGround;
   public double maxFallY;
   public boolean expectingFallDamage;

   public NoFallPlayerData() {
   }

}
