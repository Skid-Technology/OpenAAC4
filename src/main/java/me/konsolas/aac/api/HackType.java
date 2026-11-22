package me.konsolas.aac.api;


public enum HackType {
   CRITICALS("combat.Criticals", "criticals"),
   HITBOX("combat.Hitbox", "hitbox"),
   FASTBOW("combat.FastBow", "fastbow"),
   FIGHTSPEED("combat.FightSpeed", "fightspeed"),
   REGEN("combat.Regen", "regen"),
   FASTUSE("combat.FastUse", "fastuse"),
   KILLAURA("combat.Killaura", "killaura"),
   TIMER("movement.Timer", "timer"),
   PHASE("movement.Phase", "phase"),
   MOVE("movement.Move", "move"),
   NOFALL("movement.NoFall", "nofall"),
   DERP("movement.Derp", "derp"),
   BADPACKETS("world.BadPackets", "badpackets"),
   FASTBREAK("world.FastBreak", "fastbreak"),
   FASTPLACE("world.FastPlace", "fastplace"),
   INTERACT("world.Interact", "interact");

   private final String checkPath;
   private final String name;
   private final int id;

   HackType(String str, String str2) {
      this.checkPath = str;
      this.name = str2;
      this.id = this.ordinal();
   }

   @Override
   public String toString() {
      return this.checkPath;
   }

   public String getName() {
      return this.name;
   }

   public int getId() {
      return this.id;
   }


}
