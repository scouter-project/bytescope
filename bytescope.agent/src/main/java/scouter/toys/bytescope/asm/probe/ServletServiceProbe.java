/*
 *  Copyright 2015 the original author or authors. 
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */
package scouter.toys.bytescope.asm.probe;

import scouter.org.objectweb.asm.*;
import scouter.org.objectweb.asm.commons.LocalVariablesSorter;
import scouter.toys.bytescope.asm.ClassDesc;
import scouter.toys.bytescope.asm.IASM;
import scouter.toys.bytescope.deco.CommonDeco;
import scouterx.org.pmw.tinylog.Logger;

import java.util.HashSet;

public class ServletServiceProbe implements IASM, Opcodes {
	public HashSet<String> servlets = new HashSet<String>();

	public ServletServiceProbe() {
		servlets.add("javax/servlet/http/HttpServlet");
	}

	public ClassVisitor transform(ClassVisitor cv, String className, ClassDesc classDesc) {
		//TODO check transform

		if (servlets.contains(className)) {
			return new ServletServiceCV(cv, className);
		}

		//TODO transform the case of servlet filter

		return cv;
	}
}

class ServletServiceCV extends ClassVisitor implements Opcodes {
	private static String TARGET_SERVICE = "service";
	private static String TARGET_SIGNATURE = "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;";
	private String className;
	public ServletServiceCV(ClassVisitor cv, String className) {
		super(ASM4, cv);
		this.className = className;
	}
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if (mv == null) {
			return mv;
		}
		if (desc.startsWith(TARGET_SIGNATURE)) {
			if (TARGET_SERVICE.equals(name)) {
				Logger.info("HTTP service instrumented - " + className);
				return new ServletServiceMV(access, desc, mv);
			}
		}
		return mv;
	}
}

// ///////////////////////////////////////////////////////////////////////////
class ServletServiceMV extends LocalVariablesSorter implements Opcodes {
	private static final String DECO = CommonDeco.class.getName().replace('.', '/');
	private final static String START_SERVICE = "startHttpService";
	private static final String START_SIGNATURE = "(Ljava/lang/Object;Ljava/lang/Object;)V";
	private final static String END_METHOD = "endHttpService";
	private static final String END_SIGNATURE = "(Ljava/lang/Throwable;)V";
    private Label labelTry = new Label();

	public ServletServiceMV(int access, String desc, MethodVisitor mv) {
		super(ASM4, access, desc, mv);
	}
	private int statIdx;
	@Override
	public void visitCode() {
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitVarInsn(Opcodes.ALOAD, 2);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, DECO, START_SERVICE, START_SIGNATURE, false);

        mv.visitLabel(labelTry);
        mv.visitCode();

	}

	@Override
	public void visitInsn(int opcode) {
		if ((opcode >= IRETURN && opcode <= RETURN)) {
			mv.visitVarInsn(Opcodes.ALOAD, statIdx);
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, DECO, END_METHOD, END_SIGNATURE, false);
		}
		mv.visitInsn(opcode);
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		Label labelCatch = new Label();
		mv.visitTryCatchBlock(labelTry, labelCatch, labelCatch, null);
		mv.visitLabel(labelCatch);
        mv.visitInsn(DUP);
        int errIdx = newLocal(Type.getType(Throwable.class));
        mv.visitVarInsn(Opcodes.ASTORE, errIdx);
        mv.visitVarInsn(Opcodes.ALOAD, errIdx);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, DECO, END_METHOD, END_SIGNATURE, false);
		mv.visitInsn(ATHROW);
		mv.visitMaxs(maxStack + 3, maxLocals + 1);
	}
}
