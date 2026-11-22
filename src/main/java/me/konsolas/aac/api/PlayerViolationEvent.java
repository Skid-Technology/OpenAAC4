package me.konsolas.aac.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerViolationEvent extends Event implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   private final Player player;
   private boolean cancelled;
   private final HackType hackType;
   private int violations;
   private final String message;

   public PlayerViolationEvent(Player player, HackType hackType, int i, String str) {
      super(!Bukkit.isPrimaryThread());
      this.player = player;
      this.hackType = hackType;
      this.violations = i;
      this.message = str;
   }

   public static HandlerList getHandlerList() {
      return handlerList;
   }

   public Player getPlayer() {
      return this.player;
   }

   public int getViolations() {
      return this.violations;
   }

   public void setViolations(int i) {
      this.violations = i;
   }

   public HackType getHackType() {
      return this.hackType;
   }

   public String getMessage() {
      return this.message;
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
