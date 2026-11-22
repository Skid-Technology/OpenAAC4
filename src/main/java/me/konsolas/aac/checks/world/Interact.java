package me.konsolas.aac.checks.world;


import me.konsolas.aac.checks.Check;
import me.konsolas.aac.checks.CheckManager;
import me.konsolas.aac.checks.PlayerData;
import me.konsolas.aac.compat.PlayerStateAccess;
import me.konsolas.aac.compat.ProtocolVersion;
import me.konsolas.aac.compat.WorldRayTraceAccess;
import me.konsolas.aac.util.BlockRayTraceResult;
import me.konsolas.aac.util.MaterialGroup;
import me.konsolas.aac.util.VectorFormatter;
import com.comphenix.protocol.PacketType.Play.Client;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import me.konsolas.aac.api.HackType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class Interact extends Check {
   private static final Set HORIZONTAL_FACES = Collections.unmodifiableSet(EnumSet.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST));

   public Interact(CheckManager checkManager) {
      super(checkManager, HackType.INTERACT, () -> new InteractPlayerData(null), Collections.singletonList(Client.BLOCK_PLACE), true, (player, data) -> true);
   }

   @Override
   public void onPlayerInteract(Player player, PlayerData playerData, PlayerInteractEvent event) {
      if (event.getClickedBlock() != null && event.getClickedBlock().getType() != Material.AIR) {
         if (player.isDead()) {
            String str = "module.dead";
            int value = this.getIntOption(str);
            if (value >= 0) {
               Player player2 = event.getPlayer();
               String str2 = "interacted whilst dead";
               int value2 = value;
               Player player3 = player2;
               if (this.addViolation(player3, value2, str2)) {
                  event.setCancelled(true);
               }
            }
         }

         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_14) && event.getMaterial() == Material.SCAFFOLDING) {
               return;
            }

            if (event.getMaterial().isBlock() && event.getMaterial() != Material.AIR) {
               String str3 = "module.block_place";
               int value3 = this.getIntOption(str3);
               if (value3 >= 0 && !this.hasLineOfSight(player, event.getClickedBlock(), event.getBlockFace())) {
                  String str4 = "tried to place " + event.getMaterial() + " on " + event.getClickedBlock().getType() + " out of sight";
                  int value4 = value3;
                  Player player4 = player;
                  if (this.addViolation(player4, value4, str4)) {
                     event.setCancelled(true);
                  }
               }
            }

            if (MaterialGroup.SPAWN_EGGS.contains(event.getMaterial())) {
               String str5 = "module.spawn_egg";
               int value5 = this.getIntOption(str5);
               if (value5 >= 0 && !this.hasLineOfSight(player, event.getClickedBlock(), event.getBlockFace())) {
                  String str6 = "tried to spawn " + event.getMaterial() + " on block out of sight";
                  int value6 = value5;
                  Player player5 = player;
                  if (this.addViolation(player5, value6, str6)) {
                     event.setCancelled(true);
                  }
               }
            }

            if (MaterialGroup.REDSTONE.contains(event.getClickedBlock().getType())) {
               String str7 = "module.redstone";
               int value7 = this.getIntOption(str7);
               if (value7 >= 0 && !this.hasLineOfSight(player, event.getClickedBlock(), event.getBlockFace())) {
                  String str8 = "tried to interact with " + event.getClickedBlock().getType() + " out of sight";
                  int value8 = value7;
                  Player player6 = player;
                  if (this.addViolation(player6, value8, str8)) {
                     event.setCancelled(true);
                  }
               }
            }

            if (MaterialGroup.CHESTS.contains(event.getClickedBlock().getType())) {
               String str9 = "module.open_chest";
               int value9 = this.getIntOption(str9);
               if (value9 >= 0 && !this.hasLineOfSight(player, event.getClickedBlock(), event.getBlockFace())) {
                  String str10 = "tried to open a " + event.getClickedBlock().getType() + " out of sight";
                  int value10 = value9;
                  Player player7 = player;
                  if (this.addViolation(player7, value10, str10)) {
                     event.setCancelled(true);
                  }
               }
            }
         }
      }
   }

   @Override
   public void onBlockBreak(Player player, PlayerData playerData, BlockBreakEvent event) {
      String str = "module.block_break";
      int value = this.getIntOption(str);
      if (value >= 0) {
         Block block = event.getBlock();
         if (!this.hasLineOfSight(player, block, null)) {
            String str2 = "broke " + block.getType() + " out of line of sight";
            int value2 = value;
            Player player2 = player;
            if (this.addViolation(player2, value2, str2)) {
               event.setCancelled(true);
            }
         }
      }
   }

   @Override
   public void onBlockPlace(Player player, PlayerData playerData, BlockPlaceEvent event) {
      if (!ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_14) || event.getBlockPlaced().getType() != Material.SCAFFOLDING) {
         if (event.getBlockAgainst().isLiquid() && event.getBlockPlaced().getType() != MaterialGroup.LILY_PAD.createItemStack().getType()) {
            String str = "module.liquids";
            int value = this.getIntOption(str);
            if (value >= 0) {
               String str2 = "interacted with a liquid";
               int value2 = value;
               Player player2 = player;
               if (this.addViolation(player2, value2, str2)) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @Override
   public void attack(Player player, PlayerData playerData, EntityDamageByEntityEvent event) {
      String str = "module.dead";
      int value = this.getIntOption(str);
      if (value >= 0 && player.isDead()) {
         String str2 = "attacked whilst dead";
         int value2 = value;
         Player player2 = player;
         if (this.addViolation(player2, value2, str2)) {
            event.setCancelled(true);
         }
      }

      String str3 = "module.item_attack";
      value = this.getIntOption(str3);
      if (value >= 0) {
         Player player3 = player;
         if (PlayerStateAccess.isUsingItem(player3)) {
            String str4 = "attacked whilst using an item";
            int value3 = value;
            Player player4 = player;
            if (this.addViolation(player4, value3, str4)) {
               event.setCancelled(true);
            }
         }
      }
   }

   private boolean hasLineOfSight(Player player, Block block, BlockFace face) {
      Vector vector = player.getEyeLocation().toVector();
      Vector vector2 = player.getLocation().getDirection();
      BlockVector blockVec = block.getLocation().toVector().toBlockVector();
      BlockRayTraceResult blockRayTraceResult = WorldRayTraceAccess.rayTraceBlock(player, vector, vector2, 5.0);
      if (!blockRayTraceResult.isHit() || !blockRayTraceResult.getBlockPosition().equals(blockVec) || face != null && blockRayTraceResult.getHitFace() != face) {
         Location location = face == null ? block.getLocation().add(0.5, 0.5, 0.5) : this.getFaceMidpoint(block, face);
         Vector vector3 = location.toVector().subtract(vector);
         float x = (float)(Math.atan2(vector3.getZ(), vector3.getX()) * 180.0 / Math.PI) - 90.0F;
         float x2 = (float)(-(Math.atan2(vector3.getY(), Math.hypot(vector3.getX(), vector3.getZ())) * 180.0 / Math.PI));
         double pitch = Math.hypot(VectorFormatter.angleDifference(x, player.getLocation().getYaw()), x2 - player.getLocation().getPitch());
         String str = "max_angle_diff";
         if (!(pitch < this.getDoubleOption(str))) {
            return false;
         }

         BlockRayTraceResult blockRayTraceResult2 = WorldRayTraceAccess.rayTraceBlock(player, vector, vector3, 5.0);
         return blockRayTraceResult2.isHit() && blockRayTraceResult2.getBlockPosition().equals(blockVec) && (face == null || blockRayTraceResult2.getHitFace() == face);
      } else {
         return true;
      }
   }

   private Location getFaceMidpoint(Block block, BlockFace face) {
      return block.getRelative(face).getLocation().add(0.5, 0.5, 0.5).add(block.getLocation().add(0.5, 0.5, 0.5)).multiply(0.5);
   }
}
