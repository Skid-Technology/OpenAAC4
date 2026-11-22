package me.konsolas.aac.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PlayerViolationCommandEvent extends PlayerEvent implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   private boolean cancelled;
   private final HackType hackType;
   private String command;

   public PlayerViolationCommandEvent(Player player, HackType hackType, String str) {
      super(player);
      this.hackType = hackType;
      this.command = str;
   }

   public static HandlerList getHandlerList() {
      return handlerList;
   }

   public String getCommand() {
      return this.command;
   }

   public void setCommand(String str) {
      this.command = str;
   }

   public HackType getHackType() {
      return this.hackType;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean flag) {
      this.cancelled = flag;
   }

   public HandlerList getHandlers() {
      return handlerList;
   }

}
