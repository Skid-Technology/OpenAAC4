package me.konsolas.aac.compat;

import java.lang.reflect.Field;
import org.bukkit.entity.Player;

public class PlayerPingAccess {
   private static Field PING_FIELD;

   public static int getPing(Player player) {
      try {
         Object obj = (Object)ProtocolVersion.GET_ENTITY_HANDLE_METHOD.invoke((Player)player);
         if (PING_FIELD == null) {
            PING_FIELD = obj.getClass().getDeclaredField("ping");
            PING_FIELD.setAccessible(true);
         }

         int value = PING_FIELD.getInt(obj);
         return value > 0 ? value : 0;
      } catch (Throwable ex) {
         return 1;
      }
   }
}
