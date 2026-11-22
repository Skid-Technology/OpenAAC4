package me.konsolas.aac.compat;

import org.objectweb.asm.MethodVisitor;

public class MovementEpsilonVisitor extends MethodVisitor {
   private static final Double ORIGINAL_EPSILON = 0.00390625;
   private static final Double REPLACEMENT_EPSILON = 0.0;
   private boolean replaced;

   public MovementEpsilonVisitor(MethodVisitor methodVisitor) {
      super(org.objectweb.asm.Opcodes.ASM7, methodVisitor);
      this.replaced = false;
   }

   @Override
   public void visitLdcInsn(Object obj) {
      if (ORIGINAL_EPSILON.equals(obj)) {
         this.replaced = true;
         Double doubleVal = REPLACEMENT_EPSILON;
         super.visitLdcInsn(doubleVal);
      } else {
         Object obj2 = obj;
         super.visitLdcInsn(obj2);
      }
   }

   public boolean wasReplaced() {
      return this.replaced;
   }
}
