package me.konsolas.aac.gui;

import org.bukkit.entity.Player;

public class DeferredContextClickTask implements Runnable {
   public final Player player;
   public final ContextClickMenuItem clickItem;
   public final ContextClickMenuItem ownerItem;

   public DeferredContextClickTask(ContextClickMenuItem contextClickMenuItem, Player player, ContextClickMenuItem contextClickMenuItem2) {
      this.ownerItem = contextClickMenuItem;
      this.player = player;
      this.clickItem = contextClickMenuItem2;
   }

   @Override
   public void run() {
      if (this.player.isOnline()) {
         ContextClickMenuItem.getClickHandler(this.ownerItem).accept(new MenuClick(this.player, this.clickItem));
      }
   }
}
