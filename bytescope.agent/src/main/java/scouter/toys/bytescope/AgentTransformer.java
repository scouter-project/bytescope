package scouter.toys.bytescope;

import scouter.org.objectweb.asm.*;
import scouter.toys.bytescope.asm.AsmUtil;
import scouter.toys.bytescope.asm.ClassDesc;
import scouter.toys.bytescope.asm.IASM;
import scouter.toys.bytescope.asm.ScouterClassWriter;
import scouter.toys.bytescope.asm.probe.MethodBeforeProbe;
import scouter.toys.bytescope.asm.probe.ServletServiceProbe;
import scouter.toys.bytescope.util.FileUtil;
import scouterx.org.pmw.tinylog.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 4.
 */
public class AgentTransformer implements ClassFileTransformer {
    public static ThreadLocal<ClassLoader> hookingCtx = new ThreadLocal<ClassLoader>();
    private static List<IASM> probes = new ArrayList<IASM>();
    private static int hook_signature;

    static {
        reload();
    }

    public static void reload() {
        List<IASM> temp = new ArrayList<IASM>();
        temp.add(new MethodBeforeProbe());
        temp.add(new ServletServiceProbe());

        probes = temp;
    }

    private AgentConfigure conf = AgentConfigure.getInstance();

    public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            hookingCtx.set(loader);
            if (className == null)
                return null;

            if (classBeingRedefined == null) {
                return null;
            }

            if (className.startsWith("scouter/")) {
                return null;
            }

            final ClassDesc classDesc = new ClassDesc();
            ClassReader cr = new ClassReader(classfileBuffer);
            cr.accept(new ClassVisitor(Opcodes.ASM4) {
                public void visit(int version, int access, String name, String signature, String superName,
                                  String[] interfaces) {
                    classDesc.set(version, access, name, signature, superName, interfaces);
                    super.visit(version, access, name, signature, superName, interfaces);
                }

                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    classDesc.anotation += desc;
                    return super.visitAnnotation(desc, visible);
                }
            }, 0);
            if (AsmUtil.isInterface(classDesc.access)) {
                return null;
            }
            classDesc.classBeingRedefined = classBeingRedefined;
            ClassWriter cw = getClassWriter(classDesc);
            ClassVisitor cv = cw;
            List<IASM> workingProbes = probes;
            for (int i = 0, max = workingProbes.size(); i < max; i++) {
                cv = workingProbes.get(i).transform(cv, className, classDesc);
                if (cv != cw) {
                    cr = new ClassReader(classfileBuffer);
                    cr.accept(cv, ClassReader.EXPAND_FRAMES);
                    classfileBuffer = cw.toByteArray();
                    cv = cw = getClassWriter(classDesc);
                    Logger.info(className + "\t[" + loader + "]");
                }
            }
            return classfileBuffer;
        } catch (Throwable t) {
            Logger.error(t, "Transformer Error" + t.getMessage());
        } finally {
            hookingCtx.set(null);
        }
        return null;
    }

    private ClassWriter getClassWriter(final ClassDesc classDesc) {
        ClassWriter cw;
        switch (classDesc.version) {
            case Opcodes.V1_1:
            case Opcodes.V1_2:
            case Opcodes.V1_3:
            case Opcodes.V1_4:
            case Opcodes.V1_5:
            case Opcodes.V1_6:
                cw = new ScouterClassWriter(ClassWriter.COMPUTE_MAXS);
                break;
            default:
                cw = new ScouterClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        }
        return cw;
    }

    private void dump(String className, byte[] bytes) {
        String fname = "/tmp/" + className.replace('/', '_');
        FileUtil.save(fname, bytes);
    }
}
