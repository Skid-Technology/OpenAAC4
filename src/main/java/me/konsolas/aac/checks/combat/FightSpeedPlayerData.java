package me.konsolas.aac.checks.combat;


import me.konsolas.aac.checks.CheckPlayerData;
public class FightSpeedPlayerData extends CheckPlayerData {
   public AttackSampleWindow hitWindow = new AttackSampleWindow();
   public AttackSampleWindow swingWindow = new AttackSampleWindow();
   public long lastCheckMillis;

   public FightSpeedPlayerData() {
   }

}
