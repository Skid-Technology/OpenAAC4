package me.konsolas.aac.movement;

public class HorizontalVector {
   public static final HorizontalVector MIN_PUSH = new HorizontalVector(Math.sqrt(0.001953125));
   public static final HorizontalVector UNIT = new HorizontalVector(1.0, 1.0);
   public static final HorizontalVector ZERO = new HorizontalVector(0.0, 0.0);
   public static final HorizontalVector WALK_REFERENCE = new HorizontalVector(0.21, 0.21);
   public static final HorizontalVector STEP_REFERENCE = new HorizontalVector(0.1, 0.1);
   private final double x;
   private final double z;
   private final double magnitude;

   public HorizontalVector(double d) {
      this(d, d);
   }

   public HorizontalVector(double d, double d2) {
      this.x = d;
      this.z = d2;
      this.magnitude = Math.sqrt(d * d + d2 * d2);
   }

   public HorizontalVector add(HorizontalVector horizontalVector) {
      return new HorizontalVector(this.x + horizontalVector.x, this.z + horizontalVector.z);
   }

   public double magnitude() {
      return this.magnitude;
   }
}
