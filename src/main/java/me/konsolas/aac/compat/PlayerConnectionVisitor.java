package me.konsolas.aac.compat;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import java.util.HashSet;
import java.util.Set;

public class PlayerConnectionVisitor extends ClassVisitor {
   private final String PLAYER_FLYING_DESC;
   private final String VEHICLE_MOVE_DESC;
   private boolean foundTargetMethod;
   private final Set<MovementEpsilonVisitor> methodVisitors;

   public PlayerConnectionVisitor(ClassVisitor classVisitor, String str) {
      super(org.objectweb.asm.Opcodes.ASM7, classVisitor);
      this.methodVisitors = new HashSet<>();
      this.PLAYER_FLYING_DESC = "(Lnet/minecraft/server/" + str + "/PacketPlayInFlying;)V";
      this.VEHICLE_MOVE_DESC = "(Lnet/minecraft/server/" + str + "/PacketPlayInVehicleMove;)V";
   }

   @Override
   public MethodVisitor visitMethod(int i, String str, String str2, String str3, String[] strArray) {
      String[] strArray2 = strArray;
      String str4 = str3;
      String str5 = str2;
      String str6 = str;
      int value = i;
      MethodVisitor methodVisitor = super.visitMethod(value, str6, str5, str4, strArray2);
      if (!"a".equals(str) || !this.PLAYER_FLYING_DESC.equals(str2) && !this.VEHICLE_MOVE_DESC.equals(str2)) {
         return methodVisitor;
      }

      this.foundTargetMethod = true;
      MethodVisitor methodVisitor2 = methodVisitor;
      MovementEpsilonVisitor movementEpsilonVisitor = new MovementEpsilonVisitor(methodVisitor2);
      this.methodVisitors.add(movementEpsilonVisitor);
      return movementEpsilonVisitor;
   }

   public boolean wasPatched() {
      return this.foundTargetMethod && this.methodVisitors.stream().allMatch(MovementEpsilonVisitor::wasReplaced);
   }
}
