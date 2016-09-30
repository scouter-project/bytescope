package scouter.toys.bytescope.mbean;

import scouterx.org.pmw.tinylog.Configurator;
import scouterx.org.pmw.tinylog.Level;
import scouterx.org.pmw.tinylog.Logger;
import scouterx.org.pmw.tinylog.labelers.ProcessIdLabeler;
import scouterx.org.pmw.tinylog.policies.SizePolicy;
import scouterx.org.pmw.tinylog.writers.RollingFileWriter;
import scouterx.toys.bytescope.common.JmxConstant;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 11.
 */
public class BytescopeContext implements BytescopeContextMBean {
    boolean loaded = false;
    String logFilePath = "";
    String agentProcessName = "";
    String currentLogLevel = "";

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
                .level(Level.INFO)
                .activate();

        Logger.info("agent log path configured - " + file);
    }

    @Override
    public String getLogFilePath(String file) {
        return logFilePath;
    }

    @Override
    public void setLogLevel(String level) {
        Level _level = Level.ERROR;

        if (JmxConstant.LOG_LEVEL_ERROR.equals(level)) {
            _level = Level.ERROR;
        } else if (JmxConstant.LOG_LEVEL_WARN.equals(level)) {
            _level = Level.WARNING;
        } else if (JmxConstant.LOG_LEVEL_INFO.equals(level)) {
            _level = Level.INFO;
        } else if (JmxConstant.LOG_LEVEL_DEBUG.equals(level)) {
            _level = Level.DEBUG;
        } else if (JmxConstant.LOG_LEVEL_TRACE.equals(level)) {
            _level = Level.TRACE;
        }
        currentLogLevel = level;
        Configurator.currentConfig()
                .level(_level)
                .activate();
    }

    @Override
    public String getLogLevel() {
        return currentLogLevel;
    }

    public void setLoaded(boolean load) {
        this.loaded = load;
    }

}
