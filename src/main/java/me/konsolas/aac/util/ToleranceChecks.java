package me.konsolas.aac.util;

public class ToleranceChecks {
   private static boolean belowUpperBound(double d, double d2, double d3) {
      return d < d2 + d3;
   }

   private static boolean aboveLowerBound(double d, double d2, double d3) {
      return d > d2 - d3;
   }

   public static boolean isWithinTolerance(double d, double d2, double d3) {
      return belowUpperBound(d, d2, d3) && aboveLowerBound(d, d2, d3);
   }
}
