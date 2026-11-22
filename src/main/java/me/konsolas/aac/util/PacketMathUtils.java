package me.konsolas.aac.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class PacketMathUtils {
   public static long gcd(long ticks, long ticks2) {
      return ticks2 == 0L ? ticks : gcd(ticks2, ticks % ticks2);
   }

   public static float wrapYawDelta(float f, float f2) {
      float f3 = (f2 - f) % 360.0F;
      return f3 > 180.0F ? 360.0F - f3 : f3;
   }

   public static double round(double d, int i) {
      if (i < 0) {
         throw new IllegalArgumentException();
      }

      BigDecimal bigDecimal = new BigDecimal(d);
      bigDecimal = bigDecimal.setScale(i, RoundingMode.HALF_UP);
      return bigDecimal.doubleValue();
   }

   public static boolean isMovementPacket(PacketType packetType) {
      return packetType == Client.FLYING || packetType == Client.POSITION || packetType == Client.POSITION_LOOK || packetType == Client.LOOK;
   }

   public static boolean isAttackPacket(PacketContainer packet) {
      return packet.getType() != Client.USE_ENTITY ? false : packet.getEntityUseActions().read(0) == EntityUseAction.ATTACK;
   }
}
