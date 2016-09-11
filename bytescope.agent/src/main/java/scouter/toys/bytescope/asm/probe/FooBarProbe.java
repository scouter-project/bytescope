package scouter.toys.bytescope.asm.probe;

import scouter.org.objectweb.asm.ClassVisitor;
import scouter.org.objectweb.asm.MethodVisitor;
import scouter.org.objectweb.asm.Opcodes;
import scouter.org.objectweb.asm.commons.LocalVariablesSorter;
import scouter.toys.bytescope.asm.ClassDesc;
import scouter.toys.bytescope.asm.HookingSet;
import scouter.toys.bytescope.asm.IASM;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 4.
 */
public class FooBarProbe implements IASM, Opcodes {

    public ClassVisitor transform(ClassVisitor cv, String className, ClassDesc classDesc) {
        if(className.equals("scouterx/bytescope/testapp1/FooBar")) {
            return new MethodInjectCV(cv, className);
        } else {
            return cv;
        }
    }

    class MethodInjectCV extends ClassVisitor implements Opcodes {

        public String className;
        private HookingSet mset;

        public MethodInjectCV(ClassVisitor cv, String className) {
            super(ASM4, cv);
            this.className = className;
        }

        @Override
        public MethodVisitor visitMethod(int access, String methodName, String desc, String signature,
                                         String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, methodName, desc, signature, exceptions);
            if (mv == null || !methodName.equals("getFooAge")) {
                return mv;
            }
            return new MethodInjectMV(access, desc, mv);
        }
    }

    class MethodInjectMV extends LocalVariablesSorter implements Opcodes {
        public MethodInjectMV(int access, String desc, MethodVisitor mv) {
            super(ASM4, access, desc, mv);
        }

        @Override
        public void visitCode() {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitIntInsn(BIPUSH, 20);
            mv.visitFieldInsn(PUTFIELD, "scouterx/bytescope/testapp1/FooBar", "fooAge", "I");
            mv.visitCode();
        }
    }
}