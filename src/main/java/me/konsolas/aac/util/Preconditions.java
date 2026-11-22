package me.konsolas.aac.util;

public final class Preconditions {
   public static Object requireNonNull(Object obj, String str) {
      if (obj == null) {
         throw new NullPointerException(str + " cannot be null");
      } else {
         return obj;
      }
   }

   public static void requireSupported(boolean flag, String str) {
      if (!flag) {
         throw new UnsupportedOperationException(str);
      }
   }

}
