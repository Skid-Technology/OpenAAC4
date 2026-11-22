package me.konsolas.aac.compat;


import me.konsolas.aac.AAC;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import protocolsupport.api.ProtocolSupportAPI;
import us.myles.ViaVersion.api.Via;

public enum ProtocolVersion {
   V1_8(1, 8),
   V1_9(1, 9),
   V1_10(1, 10),
   V1_11(1, 11),
   V1_12(1, 12),
   V1_13(1, 13),
   V1_14(1, 14),
   V1_15(1, 15);

   private static final Pattern VERSION_PATTERN = Pattern.compile(".*\\(.*MC.\\s*([a-zA-z0-9\\-.]+).*");
   private static final ProtocolVersion SERVER_VERSION;
   private final int majorVersion;
   private final int minorVersion;
   public static final Class CRAFT_ENTITY_CLASS = ReflectionUtils.getCraftEntityClass();
   public static final Class CRAFT_PLAYER_CLASS = ReflectionUtils.getCraftPlayerClass();
   public static final MethodHandle GET_ENTITY_HANDLE_METHOD;
   private static final boolean VIA_VERSION_ENABLED;
   private static final boolean PROTOCOL_SUPPORT_ENABLED;

   private ProtocolVersion(int i, int i2) {
      this.majorVersion = i;
      this.minorVersion = i2;
   }

   public int getMajorVersion() {
      return this.majorVersion;
   }

   public int getMinorVersion() {
      return this.minorVersion;
   }

   public static ProtocolVersion fromPlayer(Player player) {
      if (VIA_VERSION_ENABLED) {
         int value = Via.getAPI().getPlayerVersion(player);
         return fromProtocolId(value);
      } else if (PROTOCOL_SUPPORT_ENABLED) {
         int value2 = ProtocolSupportAPI.getProtocolVersion(player).getId();
         return fromProtocolId(value2);
      } else {
         return getServerVersion();
      }
   }

   public static ProtocolVersion fromProtocolId(int i) {
      if (550 <= i && i <= 578) {
         return V1_15;
      } else if (441 <= i && i <= 498) {
         return V1_14;
      } else if (341 <= i && i <= 404) {
         return V1_13;
      } else if (317 <= i && i <= 340) {
         return V1_12;
      } else if (301 <= i && i <= 316) {
         return V1_11;
      } else if (201 <= i && i <= 210) {
         return V1_10;
      } else if (48 <= i && i <= 110) {
         return V1_9;
      } else if (6 <= i && i <= 47) {
         return V1_8;
      } else if (i <= 5) {
         AAC.getInstance().getLogger().warning("Unsupported protocol version: " + i + " (assuming 1.8)");
         return V1_8;
      } else {
         AAC.getInstance().getLogger().warning("Unsupported protocol version: " + i + " (assuming 1.15)");
         ProtocolVersion protocolVersion = V1_15;
         return protocolVersion;
      }
   }

   public static ProtocolVersion getServerVersion() {
      return SERVER_VERSION;
   }

   public boolean isAtLeast(ProtocolVersion protocolVersion) {
      return protocolVersion != null && this.compareTo(protocolVersion) >= 0;
   }

   @Override
   public String toString() {
      return this.majorVersion + "." + this.minorVersion;
   }

   static {
      try {
         Lookup lookup = ReflectionAccess.LOOKUP;
         Class classVal = CRAFT_ENTITY_CLASS;
         Class[] classValArray = new Class[0];
         GET_ENTITY_HANDLE_METHOD = lookup.unreflect(classVal.getMethod("getHandle", classValArray));
      } catch (NoSuchMethodException | IllegalAccessException ex) {
         throw new IllegalStateException("missing CraftPlayer#getHandle", ex);
      }

      PluginManager pluginManager = Bukkit.getPluginManager();
      String str = "ViaVersion";
      PluginManager pluginManager2 = pluginManager;
      VIA_VERSION_ENABLED = pluginManager2.getPlugin(str) != null;
      PluginManager pluginManager3 = Bukkit.getPluginManager();
      String str2 = "ProtocolSupport";
      PluginManager pluginManager4 = pluginManager3;
      PROTOCOL_SUPPORT_ENABLED = pluginManager4.getPlugin(str2) != null;
      Matcher matcher = VERSION_PATTERN.matcher(Bukkit.getVersion());
      if (matcher.matches() && matcher.group(1) != null) {
         String str3 = matcher.group(1);
         String[] strArray = str3.split("\\.");
         if (strArray.length < 2) {
            throw new IllegalStateException("Corrupt server version " + str3);
         }

         int value = Integer.parseInt(strArray[0]);
         int value2 = Integer.parseInt(strArray[1]);

         ProtocolVersion resolved = null;

         for (ProtocolVersion protocolVersion : values()) {
            if (protocolVersion.getMajorVersion() == value && protocolVersion.getMinorVersion() == value2) {
               resolved = protocolVersion;
               break;
            }
         }

         if (resolved == null) {
            throw new IllegalStateException("Unsupported minecraft version: " + value + "." + value2);
         }

         SERVER_VERSION = resolved;
      } else {
         throw new IllegalStateException("Failed to determine server version");
      }
   }
}
