package scouter.toys.bytescope.mbean;

import scouter.toys.bytescope.AgentTransformer;
import scouter.toys.bytescope.util.ClassUtil;
import scouterx.org.pmw.tinylog.Logger;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 19.
 */
public class BytescopeAttachment implements BytescopeAttachmentMBean {
    Instrumentation inst;

    public Instrumentation getInstrumentation() {
        return inst;
    }

    public BytescopeAttachment(Instrumentation inst) {
        this.inst = inst;
    }

    public void attachEnhancedThreadNameForServlet() {
        Logger.info("[invoked]attachEnhancedThreadNameForServlet]");
        List<String> classes = new ArrayList<String>();
        classes.add("javax/servlet/http/HttpServlet");
        redefineClasses(classes);
    }

    public void attachEnhancedThreadNameForServletFilter() {

    }

    public void redefineClasses(List<String> classNameList) {
        HashSet<String> paramSet = new HashSet<String>();
        for (int i = 0; i < classNameList.size(); i++) {
            String className = classNameList.get(i);
            paramSet.add(className);
        }

        Class[] classes = inst.getAllLoadedClasses();
        ArrayList<ClassDefinition> definitionList = new ArrayList<ClassDefinition>();
        boolean allSuccess = true;
        for (int i = 0; paramSet.size() > 0 && i < classes.length; i++) {
            if (paramSet.contains(classes[i].getName())) {
                try {
                    byte[] buff = ClassUtil.getByteCode(classes[i]);
                    if (buff == null) {
                        continue;
                    }
                    definitionList.add(new ClassDefinition(classes[i], buff));
                    paramSet.remove(classes[i].getName());
                } catch (Exception e) {
                    allSuccess = false;
                    Logger.error(e);
                    break;
                }
            }
        }
        if (definitionList.size() > 0 && allSuccess) {
            AgentTransformer transformer = new AgentTransformer();
            inst.addTransformer(transformer);

            try {
                inst.redefineClasses(definitionList.toArray(new ClassDefinition[definitionList.size()]));
            } catch (Throwable th) {
                throw new RuntimeException("Failed to redefine classes", th);
            } finally {
                inst.removeTransformer(transformer);
            }
        }
    }

    public void redefineImplementations(List<String> interfaceNameList) {
        HashSet<String> paramSet = new HashSet<String>();
        for (int i = 0; i < interfaceNameList.size(); i++) {
            String interfaceName = interfaceNameList.get(i);
            paramSet.add(interfaceName);
        }

        Class[] classes = inst.getAllLoadedClasses();
        ArrayList<ClassDefinition> definitionList = new ArrayList<ClassDefinition>();
        boolean allSuccess = true;
        for (int i = 0; paramSet.size() > 0 && i < classes.length; i++) {
            Class<?>[] interfaces = classes[i].getInterfaces();
            boolean implOk = false;
            for(Class<?> interfaze : interfaces) {
                if(paramSet.contains(interfaze.getName())) {
                    implOk = true;
                    break;
                }
            }
            if(implOk) {
                try {
                    byte[] buff = ClassUtil.getByteCode(classes[i]);
                    if (buff == null) {
                        continue;
                    }
                    definitionList.add(new ClassDefinition(classes[i], buff));
                } catch (Exception e) {
                    allSuccess = false;
                    Logger.error(e);
                    break;
                }
            }
        }

        if (definitionList.size() > 0 && allSuccess) {
            AgentTransformer transformer = new AgentTransformer();
            inst.addTransformer(transformer);

            try {
                inst.redefineClasses(definitionList.toArray(new ClassDefinition[definitionList.size()]));
            } catch (Throwable th) {
                throw new RuntimeException("Failed to redefine classes", th);
            } finally {
                inst.removeTransformer(transformer);
            }
        }
    }
}
