package me.konsolas.aac.util;

import java.util.Random;
import java.util.function.Consumer;

public class FastMath {
   public static final float SQRT_TWO = sqrt(2.0F);
   private static final float[] SIN_TABLE = initialize(new float[65536], fArray -> {
      for (int value = 0; value < fArray.length; value++) {
         fArray[value] = (float)Math.sin(value * Math.PI * 2.0 / 65536.0);
      }
   });
   private static final Random RANDOM = new Random();
   private static final int[] DE_BRUIJN_TABLE = new int[]{
      0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9
   };
   private static final double ASIN_FRACTION_BIAS = Double.longBitsToDouble(4805340802404319232L);
   private static final double[] ASIN_TABLE = new double[257];
   private static final double[] COS_ASIN_TABLE = new double[257];

   public static <T> T initialize(T obj, Consumer<T> consumer) {
      consumer.accept(obj);
      return obj;
   }

   public static float sin(float f) {
      return SIN_TABLE[(int)(f * 10430.378F) & 65535];
   }

   public static float cos(float f) {
      return SIN_TABLE[(int)(f * 10430.378F + 16384.0F) & 65535];
   }

   public static float sqrt(float f) {
      return (float)Math.sqrt(f);
   }

   public static float sqrt(double d) {
      return (float)Math.sqrt(d);
   }

   static {
      for (int value = 0; value < 257; value++) {
         double d = value / 256.0;
         double d2 = Math.asin(d);
         COS_ASIN_TABLE[value] = Math.cos(d2);
         ASIN_TABLE[value] = d2;
      }
   }
}
