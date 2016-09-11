package scouter.toys.bytescope;

import scouter.lang.conf.ConfObserver;
import scouter.org.objectweb.asm.*;
import scouter.toys.bytescope.asm.AsmUtil;
import scouter.toys.bytescope.asm.ClassDesc;
import scouter.toys.bytescope.asm.IASM;
import scouter.toys.bytescope.asm.ScouterClassWriter;
import scouter.toys.bytescope.asm.probe.FooBarProbe;
import scouter.toys.bytescope.util.AsyncRunner;
import scouter.toys.bytescope.util.AgentLogger;
import scouter.util.FileUtil;
import scouter.util.IntSet;

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
    private static List<IASM> asms = new ArrayList<IASM>();
    private static int hook_signature;

    static {
        final AgentConfigure conf = AgentConfigure.getInstance();
        reload();
        hook_signature = conf.getHookSignature();
        ConfObserver.add("AgentTransformer", new Runnable() {
            public void run() {
                if (conf.getHookSignature() != hook_signature) {
                    reload();
                }
                hook_signature = conf.getHookSignature();
            }
        });
    }

    public static void reload() {
        AgentConfigure conf = AgentConfigure.getInstance();
        List<IASM> temp = new ArrayList<IASM>();
        temp.add(new FooBarProbe());

        asms = temp;
    }

    // //////////////////////////////////////////////////////////////
    // boot class이지만 Hooking되어야하는 클래스를 등록한다.
    private static IntSet asynchook = new IntSet();

    static {
        asynchook.add("sun/net/www/protocol/http/HttpURLConnection".hashCode());
        asynchook.add("sun/net/www/http/HttpClient".hashCode());
        asynchook.add("java/net/Socket".hashCode());
        asynchook.add("javax/naming/InitialContext".hashCode());
    }

    private AgentConfigure conf = AgentConfigure.getInstance();
    private AgentLogger.FileLog bciOut;

    public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            hookingCtx.set(loader);
            if (className == null)
                return null;
            if (classBeingRedefined == null) {
                if (asynchook.contains(className.hashCode())) {
                    AsyncRunner.getInstance().add(loader, className, classfileBuffer);
                    return null;
                }
                if (loader == null ) {
                    return null;
                }
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
            List<IASM> workAsms = asms;
            for (int i = 0, max = workAsms.size(); i < max; i++) {
                cv = workAsms.get(i).transform(cv, className, classDesc);
                if (cv != cw) {
                    cr = new ClassReader(classfileBuffer);
                    cr.accept(cv, ClassReader.EXPAND_FRAMES);
                    classfileBuffer = cw.toByteArray();
                    cv = cw = getClassWriter(classDesc);
                    if (this.bciOut == null) {
                        this.bciOut = new AgentLogger.FileLog("./scouter.bci");
                    }
                    this.bciOut.println(className + "\t\t[" + loader + "]");
                }
            }
            return classfileBuffer;
        } catch (Throwable t) {
            AgentLogger.println("A101", "Transformer Error", t);
            t.printStackTrace();
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
