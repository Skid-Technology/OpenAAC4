package me.konsolas.aac.util;


import me.konsolas.aac.compat.ProtocolVersion;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum MaterialGroup {
   LILY_PAD("WATER_LILY", "LILY_PAD"),
   SKULL("SKULL_ITEM", "PLAYER_HEAD"),
   GRAY_DYE(() -> new ItemStack(Material.getMaterial("INK_SACK"), 1, (short)8), () -> new ItemStack(Material.getMaterial("GRAY_DYE"))),
   LIME_DYE(() -> new ItemStack(Material.getMaterial("INK_SACK"), 1, (short)10), () -> new ItemStack(Material.getMaterial("LIME_DYE"))),
   BONE_MEAL(() -> new ItemStack(Material.getMaterial("INK_SACK"), 1, (short)15), () -> new ItemStack(Material.getMaterial("BONE_MEAL"))),
   BOOK_AND_QUILL("BOOK_AND_QUILL", "WRITABLE_BOOK"),
   GREY_GLASS_PANE("STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE"),
   COMMAND_BLOCK("COMMAND", "COMMAND_BLOCK"),
   CLOCK("WATCH", "CLOCK"),
   GREEN_STAINED_CLAY(() -> new ItemStack(Material.getMaterial("STAINED_CLAY"), 1, (short)5), () -> new ItemStack(Material.getMaterial("GREEN_TERRACOTTA"))),
   GRAY_STAINED_CLAY(() -> new ItemStack(Material.getMaterial("STAINED_CLAY"), 1, (short)14), () -> new ItemStack(Material.getMaterial("GRAY_TERRACOTTA"))),
   FARMLAND("SOIL", "FARMLAND"),
   PISTON_BASE("PISTON_BASE", "PISTON"),
   PISTON_STICKY_BASE("PISTON_STICKY_BASE", "STICKY_PISTON"),
   PISTON_EXTENSION("PISTON_EXTENSION", "PISTON_HEAD"),
   PISTON_MOVING("PISTON_MOVING_PIECE", "MOVING_PISTON"),
   COBWEB("WEB", "COBWEB"),
   FIREWORK("FIREWORK", "FIREWORK_ROCKET");

   public static final Set<Material> TRAPDOORS;
   public static final Set<Material> SPAWN_EGGS;
   public static final Set<Material> REDSTONE;
   public static final Set<Material> BEDS;
   public static final Set<Material> SHULKER_BOXES;
   public static final Set<Material> CHESTS;
   public static final Set<Material> WATER;
   public static final Set<Material> LAVA;
   public static final Set<Material> ICE;
   public static final Set<Material> CARPET;
   public static final Set<Material> HONEY;
   private final Supplier<ItemStack> itemSupplier;
   private final Material material;

   private MaterialGroup(Supplier<ItemStack> supplier, Supplier<ItemStack> supplier2) {
      if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13)) {
         this.itemSupplier = supplier2;
      } else {
         this.itemSupplier = supplier;
      }

      this.material = this.itemSupplier.get().getType();
   }

   private MaterialGroup(String str, String str2) {
      this(() -> new ItemStack(Material.getMaterial(str)), () -> new ItemStack(Material.getMaterial(str2)));
   }

   public ItemStack createItemStack() {
      return this.itemSupplier.get();
   }

   public Material getMaterial() {
      return this.material;
   }

   static {
      EnumSet<Material> set = EnumSet.noneOf(Material.class);
      EnumSet<Material> set2 = EnumSet.noneOf(Material.class);
      EnumSet<Material> set3 = EnumSet.noneOf(Material.class);
      EnumSet<Material> set4 = EnumSet.noneOf(Material.class);
      EnumSet<Material> set5 = EnumSet.noneOf(Material.class);
      EnumSet<Material> set6 = EnumSet.noneOf(Material.class);
      EnumSet<Material> set7 = EnumSet.noneOf(Material.class);
      EnumSet<Material> set8 = EnumSet.noneOf(Material.class);
      EnumSet<Material> set9 = EnumSet.noneOf(Material.class);
      EnumSet<Material> set10 = EnumSet.noneOf(Material.class);
      EnumSet<Material> set11 = EnumSet.noneOf(Material.class);

      for (Material material : Material.values()) {
         if (material.name().contains("TRAPDOOR") || material.name().contains("TRAP_DOOR")) {
            set.add(material);
            set3.add(material);
         }

         if (material.name().contains("SPAWN_EGG")) {
            set2.add(material);
         }

         if (material.name().contains("DOOR")) {
            set3.add(material);
         }

         if (material.name().contains("FENCE_GATE")) {
            set3.add(material);
         }

         if (material.name().contains("BUTTON")) {
            set3.add(material);
         }

         if (material.name().contains("REPEATER")) {
            set3.add(material);
         }

         if (material.name().contains("DIODE")) {
            set3.add(material);
         }

         if (material.name().contains("COMPARATOR")) {
            set3.add(material);
         }

         if (material.name().contains("BED")) {
            set4.add(material);
         }

         if (material.name().contains("SHULKER_BOX")) {
            set5.add(material);
         }

         if (material.name().endsWith("CHEST")) {
            set6.add(material);
         }

         if (material.name().endsWith("WATER")) {
            set7.add(material);
         }

         if (material.name().endsWith("LAVA")) {
            set8.add(material);
         }

         if (material.name().endsWith("ICE")) {
            set9.add(material);
         }

         if (material.name().contains("CARPET")) {
            set10.add(material);
         }

         if (material.name().contains("HONEY_BLOCK")) {
            set11.add(material);
         }
      }

      TRAPDOORS = Collections.unmodifiableSet(set);
      SPAWN_EGGS = Collections.unmodifiableSet(set2);
      REDSTONE = Collections.unmodifiableSet(set3);
      BEDS = Collections.unmodifiableSet(set4);
      SHULKER_BOXES = Collections.unmodifiableSet(set5);
      CHESTS = Collections.unmodifiableSet(set6);
      WATER = Collections.unmodifiableSet(set7);
      LAVA = Collections.unmodifiableSet(set8);
      ICE = Collections.unmodifiableSet(set9);
      CARPET = Collections.unmodifiableSet(set10);
      HONEY = Collections.unmodifiableSet(set11);
   }
}
