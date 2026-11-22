package me.konsolas.aac.punishment;

import java.util.EnumMap;
import me.konsolas.aac.api.HackType;

public final class DefaultViolationWeights extends EnumMap {
   public DefaultViolationWeights(Class classVal) {
      super(classVal);
      this.put(HackType.CRITICALS, 1.0);
      this.put(HackType.HITBOX, 1.0);
      this.put(HackType.FASTBOW, 1.0);
      this.put(HackType.FIGHTSPEED, 1.0);
      this.put(HackType.REGEN, 1.0);
      this.put(HackType.FASTUSE, 1.0);
      this.put(HackType.KILLAURA, 1.0);
      this.put(HackType.TIMER, 1.0);
      this.put(HackType.PHASE, 1.0);
      this.put(HackType.MOVE, 1.0);
      this.put(HackType.NOFALL, 1.0);
      this.put(HackType.DERP, 1.0);
      this.put(HackType.BADPACKETS, 1.0);
      this.put(HackType.FASTBREAK, 1.0);
      this.put(HackType.FASTPLACE, 1.0);
      this.put(HackType.INTERACT, 1.0);
   }
}
