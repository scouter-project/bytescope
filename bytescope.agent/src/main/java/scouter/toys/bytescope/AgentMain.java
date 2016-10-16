package scouter.toys.bytescope;

import scouter.toys.bytescope.mbean.BytescopeAttachment;
import scouter.toys.bytescope.mbean.BytescopeContext;
import scouterx.toys.bytescope.common.JmxConstant;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 4.
 */
public class AgentMain {
    private static Instrumentation instrumentation;
    private static boolean loaded = false;

    public static boolean isLoaded() {
        return loaded;
    }

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

//    public static void premain(String args, Instrumentation inst) throws Exception {
//        System.out.println("[bytescope] start premain");
//        preMainLoaded = true;
//        innermain(args, inst);
//    }

    public static void agentmain(String args, Instrumentation inst) throws Exception {
        try {
            if (loaded) {
                System.out.println("[bytescope] aleady loaded");
                return;
            }
            System.out.println("[bytescope] start agentmain");
            innermain(args, inst);

        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    private static void innermain(String args, Instrumentation inst) throws Exception {
        instrumentation = inst;
        //instrumentation.addTransformer(new AgentTransformer());

        loaded = true;

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        BytescopeContext context = new BytescopeContext();
        context.setLoaded(true);
        context.setAgentProcessName(ManagementFactory.getRuntimeMXBean().getName());

        if(args.length() > 0 && args != null && args.length() > 0) {
            context.setLogFilePath(args);
        }

        mBeanServer.registerMBean(context, new ObjectName(JmxConstant.CONTEXT));

        BytescopeAttachment attachment = new BytescopeAttachment(inst);

        mBeanServer.registerMBean(attachment, new ObjectName(JmxConstant.ATTACHEMENT));

    }
}
