package me.konsolas.aac.movement;


import me.konsolas.aac.util.GroundCollisionState;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class PlayerMovementState {
   public GroundCollisionState lastGroundState = GroundCollisionState.TRUE_NORMAL;
   public Vector lastVelocity = new Vector(0, 0, 0);
   public Vector pendingKnockback = null;
   public int knockbackTicksRemaining = 0;
   public long lastKnockbackMillis = 0L;
   public boolean inVehicle;
   public boolean blockingOrDigging;
   public BlockPosition lastDigPosition;
   public long lastDigTick;
   public Block revertedBlock;
   public long lastBlockPlaceTick;
   public long lastSetbackTick;
}
