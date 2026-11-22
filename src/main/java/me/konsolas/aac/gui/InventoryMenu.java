package me.konsolas.aac.gui;


import me.konsolas.aac.AAC;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class InventoryMenu {
   private static InventoryMenuListener listener;
   private static final HashMap<UUID, InventoryMenu> openMenus = new HashMap<>();
   private boolean enabled = true;
   private final boolean retainOnClose;
   private String title;
   private int size;
   private final ArrayList<MenuItem> items;
   private Inventory inventory;
   private InventoryMenu parentMenu;
   private boolean cancelUnhandledClicks;
   private boolean automaticRefresh;
   private MenuCallback<Object> closeHandler;
   private final Map<Integer, Integer> hotkeySlots;

   public InventoryMenu() {
      this("Inventory");
   }

   public InventoryMenu(int i) {
      this("Inventory", i);
   }

   public InventoryMenu(String str) {
      this(str, 9);
   }

   public InventoryMenu(String str, int i) {
      this(str, i, false);
   }

   public InventoryMenu(String str, int i, boolean flag) {
      this(str, i, flag, AAC.getInstance());
   }

   public InventoryMenu(String str, int i, boolean flag, Plugin plugin) {
      this.items = new ArrayList<>();
      this.parentMenu = null;
      this.cancelUnhandledClicks = true;
      this.automaticRefresh = true;
      this.hotkeySlots = new HashMap<>();
      this.title = str;
      this.size = i;
      this.retainOnClose = flag;
      this.refresh();
      if (listener == null) {
         listener = new InventoryMenuListener(this, null);
         Bukkit.getPluginManager().registerEvents(listener, plugin);
      }
   }

   public InventoryMenu addItem(MenuItem menuItem) {
      this.items.add(menuItem);
      if (this.automaticRefresh) {
         this.refresh();
      }

      return this;
   }

   public InventoryMenu refresh() {
      Object obj = null;
      this.inventory = Bukkit.createInventory(null, this.size, this.title);

      for (MenuItem menuItem : this.items) {
         if (menuItem.getSlot() >= this.size) {
            obj = System.err;
            String str = "Cannot create inventory: size: " + this.size + " position: " + menuItem.getSlot();
            PrintStream printStream = (PrintStream)obj;
            printStream.println(str);
         } else {
            if (menuItem.getHotkey() > -1) {
               this.hotkeySlots.put(menuItem.getHotkey(), menuItem.getSlot());
            }

            this.inventory.setItem(menuItem.getSlot(), menuItem.buildItemStack());
         }
      }

      return this;
   }

   public InventoryMenu open(Player player) {
      openMenus.put(player.getUniqueId(), this);
      player.openInventory(this.inventory);
      return this;
   }

   private MenuItem findItem(int i) {
      Integer intVal = this.hotkeySlots.get(i);
      if (intVal != null) {
         for (MenuItem menuItem : new ArrayList<>(this.getItems())) {
            if (menuItem.getSlot() == intVal) {
               return menuItem;
            }
         }
      }

      return null;
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
      AAC plugin = AAC.getInstance();
      InventoryCloseTask inventoryCloseTask = new InventoryCloseTask(this, event);
      long ticks = 1L;
      InventoryCloseTask inventoryCloseTask2 = inventoryCloseTask;
      AAC plugin2 = plugin;
      BukkitScheduler bukkitScheduler2 = bukkitScheduler;
      bukkitScheduler2.scheduleSyncDelayedTask(plugin2, inventoryCloseTask2, ticks);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      openMenus.remove(event.getPlayer().getUniqueId());
   }

   public void setEnabled(boolean flag) {
      this.enabled = flag;
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String str) {
      this.title = str;
   }

   public int getSize() {
      return this.size;
   }

   public boolean isCancelUnhandledClicks() {
      return this.cancelUnhandledClicks;
   }

   public boolean isRetainOnClose() {
      return this.retainOnClose;
   }

   public ArrayList<MenuItem> getItems() {
      return this.items;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public MenuCallback<Object> getCloseHandler() {
      return this.closeHandler;
   }

   public static HashMap<UUID, InventoryMenu> getOpenMenus() {
      return openMenus;
   }

   public static MenuItem findHotkeyItem(InventoryMenu inventoryMenu, int i) {
      return inventoryMenu.findItem(i);
   }
}
