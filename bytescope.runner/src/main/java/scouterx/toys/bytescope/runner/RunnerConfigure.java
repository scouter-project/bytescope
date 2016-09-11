/*
 *  Copyright 2015 the original author or authors. 
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */
package scouterx.toys.bytescope.runner;

import org.apache.commons.lang3.StringUtils;
import scouterx.org.pmw.tinylog.Configurator;
import scouterx.org.pmw.tinylog.labelers.CountLabeler;
import scouterx.org.pmw.tinylog.policies.SizePolicy;
import scouterx.org.pmw.tinylog.writers.RollingFileWriter;
import scouterx.toys.bytescope.util.JmxProxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class RunnerConfigure {
    private static RunnerConfigure instance = null;
    public static final String RUNNER_LOG_FILE_NAME = "runner.log";
    public static final String AGENT_LOG_FILE_NAME = "agent.log";

    private Properties property = new Properties();
    private File propertyFile;

    private static String bytescopeHome;

    //################################# default configuration values #############################################
    private String logRunnerLevel;
    private String logAgentLevel;
    private String logDir;

    //############################################################################################################

    static {
        bytescopeHome = System.getProperty("bytescope.home");
        if(StringUtils.isBlank(bytescopeHome)) {
            bytescopeHome = new File("./").getAbsolutePath();
        }
    }

    public final static synchronized RunnerConfigure getInstance() {
        if (instance == null) {
            instance = new RunnerConfigure();
        }
        return instance;
    }

    /**
     * sometimes call by sample application, at that time normally set some
     * properties directly
     */
    private RunnerConfigure() {
        Properties p = new Properties();
        Map args = new HashMap();
        args.putAll(System.getenv());
        args.putAll(System.getProperties());
        p.putAll(args);
        this.property = p;
        load();
    }

    private RunnerConfigure(boolean b) {
    }

    public File getPropertyFile() {
        if (propertyFile != null) {
            return propertyFile;
        }
        String configFileName = System.getProperty("bytescope.config", bytescopeHome + "/conf/default.conf");
        propertyFile = new File(configFileName.trim());
        return propertyFile;
    }

    public synchronized boolean load() {
        File file = getPropertyFile();

        Properties temp = new Properties();
        if (file.canRead()) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
                temp.load(in);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {}
                }
            }
        }

        apply();
        return true;
    }

    private void apply() {

        this.logRunnerLevel = getValue("log_runner_level", "INFO");
        this.logAgentLevel = getValue("log_agent_level", "INFO");

        String _logDir = getValue("log_dir");
        if(StringUtils.isBlank(_logDir)) {
            _logDir = bytescopeHome + "/logs";
        }
        this.setLogDir(_logDir);

    }


    public static String getBytescopeHome() {
        return bytescopeHome;
    }

    public String getLogRunnerLevel() {
        return logRunnerLevel;
    }

    public void setLogRunnerLevel(String logRunnerLevel) {
        this.logRunnerLevel = logRunnerLevel;
    }

    public String getLogAgentLevel() {
        return logAgentLevel;
    }

    public void setLogAgentLevel(String logAgentLevel) {
        this.logAgentLevel = logAgentLevel;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
        Configurator.defaultConfig()
                .writer(new RollingFileWriter(logDir + File.separator + RUNNER_LOG_FILE_NAME, 100, new CountLabeler(), new SizePolicy(10 * 1024 * 1024)))
                .formatPattern("[{level}][{date}][{class_name}] {message}")
                .activate();

        JmxProxy.sendAgentLogFileDef(logDir + File.separator + AGENT_LOG_FILE_NAME);
    }

    public String getValue(String key) {
        return StringUtils.trim(property.getProperty(key));
    }

    public String getValue(String key, String def) {
        return StringUtils.trim(property.getProperty(key, def));
    }

    public int getInt(String key, int def) {
        try {
            String v = getValue(key);
            if (v != null)
                return Integer.parseInt(v);
        } catch (Exception e) {
        }
        return def;
    }

    public int getInt(String key, int def, int min) {
        try {
            String v = getValue(key);
            if (v != null) {
                return Math.max(Integer.parseInt(v), min);
            }
        } catch (Exception e) {
        }
        return Math.max(def, min);
    }

    public long getLong(String key, long def) {
        try {
            String v = getValue(key);
            if (v != null)
                return Long.parseLong(v);
        } catch (Exception e) {
        }
        return def;
    }

    public boolean getBoolean(String key, boolean def) {
        try {
            String v = getValue(key);
            if (v != null)
                return Boolean.parseBoolean(v);
        } catch (Exception e) {
        }
        return def;
    }
}
