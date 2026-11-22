package me.konsolas.aac.checks.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttackSampleWindow {
   private final List intervalsMillis = Collections.synchronizedList(new ArrayList());
   private long lastSampleMillis = System.currentTimeMillis();

   public AttackSampleWindow() {
   }

   private void recordSample() {
      long now = System.currentTimeMillis();
      this.intervalsMillis.add(now - this.lastSampleMillis);
      this.lastSampleMillis = now;
   }

static void record(AttackSampleWindow attackSampleWindow) {
      attackSampleWindow.recordSample();
   }

   public static List getIntervals(AttackSampleWindow attackSampleWindow) {
      return attackSampleWindow.intervalsMillis;
   }
}
