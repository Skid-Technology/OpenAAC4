package me.konsolas.aac.checks.movement;


import me.konsolas.aac.AAC;
import me.konsolas.aac.movement.MovementContext;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;

public class MoveAnalysisThread extends Thread {
   private final Player playerValue;
   private final BlockingQueue blockingQueueValue;
   private final MovePlayerData movePlayerDataValue;
   public final Move moveValue;

   public MoveAnalysisThread(Move move, Player player, MovePlayerData data) {
      super("AAC-MoveCheck/" + player.getName());
      this.moveValue = move;
      this.blockingQueueValue = new SynchronousQueue();
      this.playerValue = player;
      this.movePlayerDataValue = data;
      this.start();
   }

   @Override
   public void run() {
      try {
         while (this.playerValue.isOnline()) {
            MovementContext movementContext = (MovementContext)this.blockingQueueValue.poll(1L, TimeUnit.SECONDS);
            if (movementContext != null) {
               try {
                  movementContext.getCheckData().pendingResolution = Move.evaluateMovement(this.moveValue, movementContext);
               } catch (RuntimeException ex) {
                  AAC.getInstance().getLogger().warning("Exception in " + this.getName() + " - trace follows");
                  ex.printStackTrace();
               }
            }
         }
      } catch (InterruptedException ex2) {
         this.movePlayerDataValue.analysisThread = null;
         this.blockingQueueValue.clear();
      }
   }

   public static BlockingQueue getPendingQueue(MoveAnalysisThread moveAnalysisThread) {
      return moveAnalysisThread.blockingQueueValue;
   }
}
