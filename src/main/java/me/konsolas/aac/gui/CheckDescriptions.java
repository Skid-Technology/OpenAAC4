package me.konsolas.aac.gui;


import java.util.HashMap;

public final class CheckDescriptions extends HashMap {
   public CheckDescriptions() {
      this.put("combat.Hitbox", new String[]{"§7When a player tries to hit another without looking", "§7at its hitbox"});
      this.put("combat.Criticals", new String[]{"§7When a player tries to deal extra damage whilst", "§7on the ground"});
      this.put("combat.Fastbow", new String[]{"§7When a player tries to fire arrows or use a bow too", "§7quickly"});
      this.put("combat.FightSpeed", new String[]{"§7When a player attacks or interacts with entities", "§7faster than normal"});
      this.put("combat.Knockback", new String[]{"§7When a player does not take knockback after", "§7being attacked"});
      this.put("combat.Regen", new String[]{"§7When a player tries to speed up their regeneration", "§7to become invincible"});
      this.put("combat.FastUse", new String[]{"§7When a player tries to eat/consume an item without", "§7waiting correctly"});
      this.put("combat.Killaura", new String[]{"§7When a player uses combat modifications to gain an", "§7unfair advantage over others"});
      this.put("movement.Timer", new String[]{"§7When a player tries to speed up time"});
      this.put("movement.Phase", new String[]{"§7When a player tries to move into or through a solid block"});
      this.put("movement.Move", new String[]{"§7When a player tries to move suspiciously"});
      this.put("movement.NoFall", new String[]{"§7When a player tries to avoid fall damage"});
      this.put("movement.Derp", new String[]{"§7When a player moves their head in an abnormal way"});
      this.put("world.BadPackets", new String[]{"§7When a player sends invalid packets"});
      this.put("world.FastBreak", new String[]{"§7When a player tries to break blocks too quickly"});
      this.put("world.FastPlace", new String[]{"§7When a player tries to place blocks too quickly"});
      this.put("world.Interact", new String[]{"§7When a player places blocks without looking or ", "§7attacks an entity behind a wall"});
   }
}
