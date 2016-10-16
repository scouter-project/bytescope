package scouterx.toys.bytescope.command;

import scouterx.toys.bytescope.command.support.CommandResult;
import scouterx.toys.bytescope.util.BytescopeContext;
import scouterx.toys.bytescope.util.JmxProxy;

import javax.management.MBeanServerConnection;


/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 11.
 */
public class Attach {
    public CommandResult attach(String[] args) {
        String command;
        if(args == null || args.length == 0) {
            command = "NULL";
        } else {
            command = args[0];
        }

        return CommandResult.getBuilder().setResultFail().setMessage("unkonwn command - " + args[0]).build();
    }

    public CommandResult attachThreadNameEnhancer(String[] args) {
        String pid = BytescopeContext.getCurrentPid();
        MBeanServerConnection connection = BytescopeContext.getMBeanConnection(pid);

        if(connection == null) {
            return CommandResult.getBuilder().setResultFail().setMessage("No MBean connections!").build();
        }

        JmxProxy.attachEnhancedThreadNameForServlet(connection);

        return CommandResult.getBuilder().setResultSuccess().setMessage("[attached] EnhancedThreadNameForServlet").build();
    }
}

