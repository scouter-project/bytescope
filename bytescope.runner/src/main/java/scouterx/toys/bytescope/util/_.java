package scouterx.toys.bytescope.util;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import scouterx.toys.bytescope.runner.BytescopeConsole;
import scouterx.toys.bytescope.runner.RunnerConfigure;
import sun.management.ConnectorAddressLink;

import java.io.File;
import java.io.IOException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 11.
 */
public class _ {
    private static RunnerConfigure conf = RunnerConfigure.getInstance();

    public static final String lineSeparator = System.getProperty("line.separator");

    private static final String jarFilePath = "/Users/gunlee/Documents/workspace/scouter/" +
            "scouter.toys/bytescope/bytescope.agent/target/" +
            "scouter.bytescope-0.1-SNAPSHOT.jar";

    public static void loadJavaAgent(String pid) {
        _.println("dynamically loading javaagent");

        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(jarFilePath, conf.getLogDir() + File.separator + conf.AGENT_LOG_FILE_NAME);
            vm.detach();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void println(String str) {
        BytescopeConsole.println(str);
    }

    public static String attachJmx(String pid) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(pid);
            String serviceUrl = ConnectorAddressLink.importFrom(Integer.parseInt(vm.id().trim()));
            if(serviceUrl == null) {
                String home = System.getProperty("java.home");
                String agent = home + File.separator + "jre" + File.separator + "lib"
                        + File.separator + "management-agent.jar";
                File f = new File(agent);
                if (!f.exists()) {
                    agent = home + File.separator + "lib" + File.separator +
                            "management-agent.jar";
                    f = new File(agent);
                    if (!f.exists()) {
                        throw new RuntimeException("management-agent.jar missing");
                    }
                }
                agent = f.getCanonicalPath();
                _.println("Loading " + agent + " into target VM ...");

                vm.loadAgent(agent);
                serviceUrl = ConnectorAddressLink.importFrom(Integer.parseInt(vm.id().trim()));
            }
            return serviceUrl;

        } finally {
            if(vm != null) {
                try {
                    vm.detach();
                } catch (IOException e) {}
            }
        }
    }

}
