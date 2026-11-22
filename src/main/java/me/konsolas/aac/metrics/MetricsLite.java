package me.konsolas.aac.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

public class MetricsLite {
   private boolean enabled;
   private static boolean logFailedRequests;
   private static boolean logSentData;
   private static boolean logResponseStatusText;
   private static String serverUUID;
   private final Plugin plugin;

   public MetricsLite(Plugin plugin) {
      super();
      if (plugin == null) {
         throw new IllegalArgumentException("Plugin cannot be null!");
      }

      this.plugin = plugin;
      File file = plugin.getDataFolder();
      File file2 = new File(file.getParentFile(), "bStats");
      File file3 = new File(file2, "config.yml");
      YamlConfiguration config = YamlConfiguration.loadConfiguration(file3);
      if (!config.isSet("serverUuid")) {
         config.addDefault("enabled", true);
         config.addDefault("serverUuid", UUID.randomUUID().toString());
         config.addDefault("logFailedRequests", false);
         config.addDefault("logSentData", false);
         config.addDefault("logResponseStatusText", false);
         config.options()
            .header(
               "bStats collects some data for plugin authors like how many servers are using their plugins.\nTo honor their work, you should not disable it.\nThis has nearly no effect on the server performance!\nCheck out https://bStats.org/ to learn more :)"
            )
            .copyDefaults(true);

         try {
            config.save(file3);
         } catch (IOException ex) {
         }
      }

      String str = config.getString("serverUuid");
      serverUUID = str;
      boolean flag = config.getBoolean("logFailedRequests", false);
      logFailedRequests = flag;
      boolean flag2 = config.getBoolean("enabled", true);
      MetricsLite metricsLite = this;
      metricsLite.enabled = flag2;
      boolean flag3 = config.getBoolean("logSentData", false);
      logSentData = flag3;
      boolean flag4 = config.getBoolean("logResponseStatusText", false);
      logResponseStatusText = flag4;
      MetricsLite metricsLite2 = this;
      if (metricsLite2.enabled) {
         boolean flag5 = false;

         for (Class classVal : Bukkit.getServicesManager().getKnownServices()) {
            try {
               classVal.getField("B_STATS_VERSION");
               flag5 = true;
               break;
            } catch (NoSuchFieldException ex2) {
            }
         }

         Bukkit.getServicesManager().register(MetricsLite.class, this, plugin, ServicePriority.Normal);
         if (!flag5) {
            this.startSubmitting();
         }
      }
   }

   public boolean isEnabled() {
      MetricsLite metricsLite = this;
      return metricsLite.enabled;
   }

   private void startSubmitting() {
      java.util.Timer timer = new java.util.Timer(true);
      timer.scheduleAtFixedRate(new MetricsTimerTask(this, timer), 300000L, 1800000L);
   }

   public JsonObject getPluginData() {
      JsonObject jsonObject = new JsonObject();
      MetricsLite metricsLite = this;
      String str = metricsLite.plugin.getDescription().getName();
      MetricsLite metricsLite2 = this;
      String str2 = metricsLite2.plugin.getDescription().getVersion();
      jsonObject.addProperty("pluginName", str);
      jsonObject.addProperty("pluginVersion", str2);
      jsonObject.add("customCharts", new JsonArray());
      return jsonObject;
   }

   private JsonObject getServerData() {
      int i;
      try {
         Method method = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
         i = method.getReturnType().equals(Collection.class)
            ? ((Collection)method.invoke(Bukkit.getServer())).size()
            : ((Player[])method.invoke(Bukkit.getServer())).length;
      } catch (Exception ex) {
         i = Bukkit.getOnlinePlayers().size();
      }

      int value = Bukkit.getOnlineMode() ? 1 : 0;
      String str = Bukkit.getVersion();
      String str2 = Bukkit.getName();
      String str3 = System.getProperty("java.version");
      String str4 = System.getProperty("os.name");
      String str5 = System.getProperty("os.arch");
      String str6 = System.getProperty("os.version");
      int value2 = Runtime.getRuntime().availableProcessors();
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("serverUUID", serverUUID);
      jsonObject.addProperty("playerAmount", i);
      jsonObject.addProperty("onlineMode", value);
      jsonObject.addProperty("bukkitVersion", str);
      jsonObject.addProperty("bukkitName", str2);
      jsonObject.addProperty("javaVersion", str3);
      jsonObject.addProperty("osName", str4);
      jsonObject.addProperty("osArch", str5);
      jsonObject.addProperty("osVersion", str6);
      jsonObject.addProperty("coreCount", value2);
      return jsonObject;
   }

   private void submitData() {
      JsonObject jsonObject = this.getServerData();
      JsonArray jsonArray = new JsonArray();

      for (Class<?> classVal : Bukkit.getServicesManager().getKnownServices()) {
         try {
            classVal.getField("B_STATS_VERSION");

            for (RegisteredServiceProvider<?> registeredServiceProvider : Bukkit.getServicesManager().getRegistrations(classVal)) {
               try {
                  Class classVal2 = registeredServiceProvider.getService();
                  Class[] classValArray = new Class[0];
                  Object obj = classVal2.getMethod("getPluginData", classValArray)
                     .invoke(registeredServiceProvider.getProvider());
                  if (obj instanceof JsonObject) {
                     jsonArray.add((JsonObject)obj);
                  } else {
                     try {
                        Class classVal3 = Class.forName("org.json.simple.JSONObject");
                        if (obj.getClass().isAssignableFrom(classVal3)) {
                           Method method = classVal3.getDeclaredMethod("toJSONString");
                           method.setAccessible(true);
                           String str = (String)method.invoke(obj);
                           JsonObject jsonObject2 = new JsonParser().parse(str).getAsJsonObject();
                           jsonArray.add(jsonObject2);
                        }
                     } catch (ClassNotFoundException ex) {
                        if (logFailedRequests) {
                           MetricsLite metricsLite = this;
                           metricsLite.plugin.getLogger().log(Level.SEVERE, "Encountered unexpected exception ", ex);
                        }
                     }
                  }
               } catch (NullPointerException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex2) {
               }
            }
         } catch (NoSuchFieldException ex3) {
         }
      }

      jsonObject.add("plugins", jsonArray);
      new Thread(new MetricsSubmissionTask(this, jsonObject)).start();
   }

   static void sendData(Plugin plugin, JsonObject jsonObject) throws Exception {
      if (jsonObject == null) {
         throw new IllegalArgumentException("Data cannot be null!");
      }

      if (Bukkit.isPrimaryThread()) {
         throw new IllegalAccessException("This method must not be called from the main thread!");
      }

      if (logSentData) {
         plugin.getLogger().info("Sending data to bStats: " + jsonObject.toString());
      }

      HttpsURLConnection httpsURLConnection = (HttpsURLConnection)new URL("https://bStats.org/submitData/bukkit").openConnection();
      byte[] bArray = compress(jsonObject.toString());
      httpsURLConnection.setRequestMethod("POST");
      String str = "application/json";
      String str2 = "Accept";
      HttpsURLConnection httpsURLConnection2 = httpsURLConnection;
      httpsURLConnection2.addRequestProperty(str2, str);
      String str3 = "close";
      String str4 = "Connection";
      HttpsURLConnection httpsURLConnection3 = httpsURLConnection;
      httpsURLConnection3.addRequestProperty(str4, str3);
      String str5 = "gzip";
      String str6 = "Content-Encoding";
      HttpsURLConnection httpsURLConnection4 = httpsURLConnection;
      httpsURLConnection4.addRequestProperty(str6, str5);
      String str7 = String.valueOf(bArray.length);
      String str8 = "Content-Length";
      HttpsURLConnection httpsURLConnection5 = httpsURLConnection;
      httpsURLConnection5.addRequestProperty(str8, str7);
      String str9 = "application/json";
      String str10 = "Content-Type";
      HttpsURLConnection httpsURLConnection6 = httpsURLConnection;
      httpsURLConnection6.setRequestProperty(str10, str9);
      String str11 = "MC-Server/1";
      String str12 = "User-Agent";
      HttpsURLConnection httpsURLConnection7 = httpsURLConnection;
      httpsURLConnection7.setRequestProperty(str12, str11);
      httpsURLConnection.setDoOutput(true);
      DataOutputStream dataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());
      byte[] bArray2 = bArray;
      DataOutputStream dataOutputStream2 = dataOutputStream;
      dataOutputStream2.write(bArray2);
      dataOutputStream.flush();
      dataOutputStream.close();
      InputStream inputStream = httpsURLConnection.getInputStream();
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      StringBuilder sb = new StringBuilder();

      String str13;
      while ((str13 = bufferedReader.readLine()) != null) {
         sb.append(str13);
      }

      bufferedReader.close();
      if (logResponseStatusText) {
         plugin.getLogger().info("Sent data to bStats and received response: " + sb.toString());
      }
   }

   private static byte[] compress(String str) throws java.io.IOException {
      if (str == null) {
         return null;
      }

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
      byte[] bArray = str.getBytes(StandardCharsets.UTF_8);
      GZIPOutputStream gzipOutputStream2 = gzipOutputStream;
      gzipOutputStream2.write(bArray);
      GZIPOutputStream gzipOutputStream3 = gzipOutputStream;
      gzipOutputStream3.close();
      return byteArrayOutputStream.toByteArray();
   }

   static Plugin getPlugin(MetricsLite metricsLite) {
      MetricsLite metricsLite2 = metricsLite;
      return metricsLite2.plugin;
   }

   static void submitData(MetricsLite metricsLite) {
      metricsLite.submitData();
   }

   static boolean isLogFailedRequests() {
      return logFailedRequests;
   }

   static {
      if (System.getProperty("bstats.relocatecheck") == null || !System.getProperty("bstats.relocatecheck").equals("false")) {
         String str = new String(new byte[]{111, 114, 103, 46, 98, 115, 116, 97, 116, 115, 46, 98, 117, 107, 107, 105, 116});
         String str2 = new String(new byte[]{121, 111, 117, 114, 46, 112, 97, 99, 107, 97, 103, 101});
         if (MetricsLite.class.getPackage().getName().equals(str) || MetricsLite.class.getPackage().getName().equals(str2)) {
            throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
         }
      }
   }
}
