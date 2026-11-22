package me.konsolas.aac.punishment;


import me.konsolas.aac.AAC;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class BanConfiguration {
   private final AAC plugin;
   private FileConfiguration configuration = null;
   private File file = null;

   public BanConfiguration(AAC plugin) {
      this.plugin = plugin;
   }

   public void reloadConfig() {
      if (this.file == null) {
         this.file = new File(this.plugin.getDataFolder(), "bans.yml");
      }

      this.configuration = YamlConfiguration.loadConfiguration(this.file);
      InputStreamReader inputStreamReader = new InputStreamReader(this.plugin.getResource("bans.yml"), StandardCharsets.UTF_8);
      YamlConfiguration config = YamlConfiguration.loadConfiguration(inputStreamReader);
      this.configuration.setDefaults(config);
   }

   public FileConfiguration getConfig() {
      if (this.configuration == null) {
         this.reloadConfig();
      }

      return this.configuration;
   }

   public void saveConfig() {
      if (this.configuration != null && this.file != null) {
         try {
            this.getConfig().save(this.file);
         } catch (IOException ex) {
            this.plugin.getLogger().severe("Could not save config to " + this.file);
            ex.printStackTrace();
         }
      }
   }

   public void saveDefaultConfig() {
      if (this.file == null) {
         this.file = new File(this.plugin.getDataFolder(), "bans.yml");
      }

      File file = this.file;
      if (!file.exists()) {
         this.plugin.saveResource("bans.yml", false);
      }
   }
}
