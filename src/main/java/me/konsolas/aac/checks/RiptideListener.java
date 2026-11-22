package me.konsolas.aac.checks;

import java.util.Optional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRiptideEvent;

public final class RiptideListener implements Listener {
   public final CheckManager checkManagerValue;

   public RiptideListener(CheckManager checkManager) {
      this.checkManagerValue = checkManager;
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onPlayerRiptide(PlayerRiptideEvent event) {
      Optional<PlayerData> optional = this.checkManagerValue.getPlayerData(event.getPlayer());
      if (optional.isPresent()) {
         int value = event.getItem().getEnchantmentLevel(Enchantment.RIPTIDE);

         for (Check check : CheckManager.getEnabledChecksMap(this.checkManagerValue).values()) {
            check.onRiptide(event.getPlayer(), optional.get(), value);
         }
      }
   }
}
