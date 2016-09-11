package scouter.toys.bytescope.mbean;

import scouterx.org.pmw.tinylog.Configurator;
import scouterx.org.pmw.tinylog.Logger;
import scouterx.org.pmw.tinylog.labelers.ProcessIdLabeler;
import scouterx.org.pmw.tinylog.policies.SizePolicy;
import scouterx.org.pmw.tinylog.writers.RollingFileWriter;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 11.
 */
public class BytescopeContext implements BytescopeContextMBean {
    boolean loaded = false;
    String logFilePath = "";
    String agentProcessName = "";

    @Override
    public String getAgentProcessName() {
        return agentProcessName;
    }

    public void setAgentProcessName(String agentProcessName) {
        this.agentProcessName = agentProcessName;
    }

    @Override
    public boolean getLoaded() {
        return loaded;
    }

    @Override
    public void setLogFilePath(String file) {
        this.logFilePath = file;

        Configurator.defaultConfig()
                .writer(new RollingFileWriter(logFilePath, 100, new ProcessIdLabeler(), new SizePolicy(10 * 1024 * 1024)))
                .formatPattern("[{level}][{date}][{class_name}] {message}")
                .activate();

        Logger.info("agent log path configured - " + file);
    }

    @Override
    public String getLogFilePath(String file) {
        return logFilePath;
    }

    public void setLoaded(boolean load) {
        this.loaded = load;
    }


}
