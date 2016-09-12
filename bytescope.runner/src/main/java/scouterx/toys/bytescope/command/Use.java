package scouterx.toys.bytescope.command;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import org.apache.commons.lang3.ArrayUtils;
import scouterx.toys.bytescope.command.support.AnsiPrint;
import scouterx.toys.bytescope.command.support.CommandResult;
import scouterx.toys.bytescope.common.JmxConstant;
import scouterx.toys.bytescope.runner.BytescopeConsole;
import scouterx.toys.bytescope.util.BytescopeContext;
import scouterx.toys.bytescope.util.JmxProxy;
import scouterx.toys.bytescope.util.__;

import javax.management.*;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 11.
 */
public class Use {
    public CommandResult use(String[] args) {
        if(ArrayUtils.isEmpty(args) || args[0] == null) {
            return CommandResult.getBuilder().setResultFail().setMessage("usage : use [pid]").build();
        }

        String pid = args[0];

        boolean javaAgentLoaded = false;
        MBeanServerConnection mbeanConnection = BytescopeContext.getMBeanConnection(pid);

        if(mbeanConnection != null) {
            javaAgentLoaded = isLoadedJavaAgent(pid);
        } else {
            try {
                String address = __.attachJmx(pid);
                JMXServiceURL jmxUrl = new JMXServiceURL(address);
                mbeanConnection = JMXConnectorFactory.connect(jmxUrl).getMBeanServerConnection();
                BytescopeContext.putMBeanConnection(pid, mbeanConnection);

            } catch (MalformedURLException e) {
                return CommandResult.getBuilder().setResultFail().setMessage(e.getMessage()).build();
            } catch (IOException e) {
                return CommandResult.getBuilder().setResultFail().setMessage(e.getMessage()).build();
            } catch (AgentInitializationException e) {
                return CommandResult.getBuilder().setResultFail().setMessage(e.getMessage()).build();
            } catch (AttachNotSupportedException e) {
                return CommandResult.getBuilder().setResultFail().setMessage(e.getMessage()).build();
            } catch (AgentLoadException e) {
                return CommandResult.getBuilder().setResultFail().setMessage(e.getMessage()).build();
            }
        }

        //load java agent
        if(!javaAgentLoaded) {
            __.loadJavaAgent(pid);
            JmxProxy.sendAgentLogFileDef(mbeanConnection);
        }

        BytescopeContext.setCurrentPid(pid);
        BytescopeConsole.getConsole().setPrompt(AnsiPrint.green("bytescope-" + pid + "> "));
        return CommandResult.getBuilder().setResultSuccess().build();
    }

    public static boolean isLoadedJavaAgent(String pid) {
        boolean javaAgentLoaded = false;
        try {
            MBeanServerConnection mbeanConnection = BytescopeContext.getMBeanConnection(pid);
            javaAgentLoaded = (Boolean) mbeanConnection.getAttribute(new ObjectName(JmxConstant.CONTEXT), JmxConstant.CONTEXT_LOADED);

        } catch (MBeanException e) {
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            // before load javaagent
        } catch (ReflectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }

        return javaAgentLoaded;
    }
}

