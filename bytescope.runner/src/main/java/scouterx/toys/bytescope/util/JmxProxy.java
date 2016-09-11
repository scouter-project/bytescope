package scouterx.toys.bytescope.util;

import scouterx.org.pmw.tinylog.Logger;
import scouterx.toys.bytescope.common.JmxConstant;
import scouterx.toys.bytescope.runner.RunnerConfigure;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.File;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 11.
 */
public class JmxProxy {

    private static RunnerConfigure conf = RunnerConfigure.getInstance();

    public static void sendAgentLogFileDef(String logDir) {
        MBeanServerConnection[] conns = BytescopeContext.getMBeanConnections();
        for(MBeanServerConnection conn : conns) {
            sendAgentLogFileDef(conn, logDir);
        }
    }

    public static void sendAgentLogFileDef(MBeanServerConnection conn, String logDir) {
        try {
            conn.setAttribute(new ObjectName(JmxConstant.CONTEXT), new Attribute(JmxConstant.CONTEXT_LOG_FILE, logDir));

            String agentProcessName = (String) conn.getAttribute(new ObjectName(JmxConstant.CONTEXT), JmxConstant.CONTEXT_AGETNT_PROCESS_NAME);

            Logger.info("send agent log dir : " + conf.getLogDir() + " to " + agentProcessName);
            System.out.println("send agent log dir : " + conf.getLogDir() + " to " + agentProcessName);

        } catch (Exception e) {
            Logger.error(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void sendAgentLogFileDef(MBeanServerConnection conn) {
        try {
            conn.setAttribute(new ObjectName(JmxConstant.CONTEXT), new Attribute(JmxConstant.CONTEXT_LOG_FILE, conf.getLogDir() + File.separator + conf.AGENT_LOG_FILE_NAME));

            String agentProcessName = (String) conn.getAttribute(new ObjectName(JmxConstant.CONTEXT), JmxConstant.CONTEXT_AGETNT_PROCESS_NAME);

            Logger.info("send agent log dir : " + conf.getLogDir() + " to " + agentProcessName);
            System.out.println("send agent log dir : " + conf.getLogDir() + " to " + agentProcessName);

        } catch (Exception e) {
            Logger.error(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
