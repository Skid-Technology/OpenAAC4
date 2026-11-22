package me.konsolas.aac.checks.movement;


import me.konsolas.aac.checks.CheckPlayerData;
import me.konsolas.aac.movement.EntityPhysicsContext;
import me.konsolas.aac.movement.MotionPrediction;
import me.konsolas.aac.movement.MovementResolution;
import me.konsolas.aac.movement.VelocityState;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class MovePlayerData extends CheckPlayerData {
   public EntityPhysicsContext lastPhysicsContext;
   public List<MotionPrediction> predictions = Collections.emptyList();
   public boolean initialized;
   public VelocityState pendingVelocity;
   public boolean stationaryPending;
   public long lastMoveTick;
   public Location lastLocation;
   public boolean usingItem;
   public boolean sprinting;
   public boolean wasSprinting;
   public boolean teleportExempt;
   public WeakReference<Entity> lastFirework;
   public int rocketBoostTicks;
   public int riptideTicks;
   public long lastPredictionFailureMillis;
   public Queue<Long> violationTimestamps = new ArrayDeque<>();
   public MoveAnalysisThread analysisThread;
   public MovementResolution pendingResolution;
   public boolean exemptMove;

   public MovePlayerData() {
   }

}
