package scouterx.toys.bytescope.util;

import com.sun.tools.attach.VirtualMachine;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 11.
 */
public class BytescopeContext {
    private static String currentPid;
    private static Map<String, VirtualMachine> jvmMap = new HashMap<String, VirtualMachine>();
    private static Map<String, MBeanServerConnection> mbeanConnectionMap = new HashMap<String, MBeanServerConnection>();

    public static String getCurrentPid() {
        return currentPid;
    }

    public static void setCurrentPid(String pid) {
        currentPid = pid;
    }

    public static VirtualMachine getVm(String pid) {
        return jvmMap.get(pid);
    }

    public static void putVm(VirtualMachine vm) {
        jvmMap.put(vm.id(), vm);
    }

    public static VirtualMachine getCurrentVm() {
        return jvmMap.get(currentPid);
    }

    public static MBeanServerConnection getMBeanConnection(String pid) {
        return mbeanConnectionMap.get(pid);
    }

    public static MBeanServerConnection[] getMBeanConnections() {
        MBeanServerConnection[] connections = new MBeanServerConnection[mbeanConnectionMap.size()];
        int i = 0;
        for (Map.Entry<String, MBeanServerConnection> entry : mbeanConnectionMap.entrySet()) {
            connections[i] = entry.getValue();
            i++;
        }
        return connections;
    }

    public static void putMBeanConnection(String pid, MBeanServerConnection connection) {
        mbeanConnectionMap.put(pid, connection);
    }

    public static MBeanServerConnection getCurrentMBeanConnection() {
        return mbeanConnectionMap.get(currentPid);
    }

    public static void close() {
        for (Map.Entry<String, VirtualMachine> entry : jvmMap.entrySet()) {
            try {
                if (entry != null) entry.getValue().detach();
            } catch (IOException e) {
            }
        }
    }
}
