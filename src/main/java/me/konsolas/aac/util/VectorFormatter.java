package me.konsolas.aac.util;

import java.util.Locale;
import org.bukkit.util.Vector;

public class VectorFormatter {
   private static final int FULL_CIRCLE_DEGREES = new VectorFormatter().parseFullCircleDegrees();

   public static double clamp(double d, double d2, double d3) {
      return Math.min(Math.max(d, d2), d3);
   }

   public static String formatVector(Vector vector) {
      return vector == null ? "null" : String.format(Locale.UK, "%.3f,%.3f,%.3f", vector.getX(), vector.getY(), vector.getZ());
   }

   public static double angleDifference(double d, double d2) {
      double d3 = Math.abs(d - d2);
      double d4 = d3 % 360.0;
      return d4 > 180.0 ? 360.0 - d4 : d4;
   }

   public static String formatDouble(double d) {
      return String.format(Locale.UK, "%.3f", d);
   }

   public int parseFullCircleDegrees() {
      return Integer.parseInt("360");
   }
}
