package me.konsolas.aac.checks.combat;

public class CombatModuleViolation {
   private final ViolationTier tier;
   private final String description;

   public CombatModuleViolation(ViolationTier violationTier, String str) {
      this.tier = violationTier;
      this.description = str;
   }

   public ViolationTier getTier() {
      return this.tier;
   }

   public String getDescription() {
      return this.description;
   }
}
