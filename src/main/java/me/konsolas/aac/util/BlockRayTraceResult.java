package me.konsolas.aac.util;

import java.util.Objects;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class BlockRayTraceResult {
   private final Vector hitPosition;
   private final BlockVector blockPosition;
   private final BlockFace hitFace;

   public BlockRayTraceResult() {
      this.hitPosition = null;
      this.blockPosition = null;
      this.hitFace = null;
   }

   public BlockRayTraceResult(Vector vector, BlockVector blockVec, BlockFace face) {
      this.hitPosition = Objects.requireNonNull(vector).clone();
      this.blockPosition = Objects.requireNonNull(blockVec);
      this.hitFace = Objects.requireNonNull(face);
   }

   public Vector getHitPosition() {
      return this.hitPosition.clone();
   }

   public BlockVector getBlockPosition() {
      return this.blockPosition;
   }

   public BlockFace getHitFace() {
      return this.hitFace;
   }

   public boolean isHit() {
      return this.hitPosition != null && this.blockPosition != null && this.hitFace != null;
   }
}
