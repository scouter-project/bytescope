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
package scouter.toys.bytescope;

import scouter.Version;
import scouter.lang.conf.ConfObserver;
import scouter.lang.conf.ConfigValueUtil;
import scouter.lang.value.ListValue;
import scouter.lang.value.MapValue;
import scouter.toys.bytescope.util.JarUtil;
import scouter.toys.bytescope.util.AgentLogger;
import scouter.util.*;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

public class AgentConfigure extends Thread {
    private static AgentConfigure instance = null;
    private long lastLoadTime = -1;
    public Properties property = new Properties();
    private boolean running = true;
    private File propertyFile;
    long lastCheck = 0;
    public static String agentDirPath;
    private int hookSignature = 0;

    private StringSet log_ignore_set = new StringSet();

    //################################# configuration properties #################################################
    public boolean _trace = false;
    public boolean _trace_use_logger = false;
    public String log_dir = "";
    public boolean log_rotation_enabled = true;
    public boolean log_sysout = false;

    public boolean hook_enabled = true;
    public String hook_injection_method_patterns;
    public boolean hook_injection_method_enabled = true;

    //############################################################################################################

    static {
        File jarFile = JarUtil.getThisJarFile();
        if (jarFile == null) {
            agentDirPath = new File("./").getAbsolutePath();
        } else {
            agentDirPath = jarFile.getParent();
        }
    }

    public final static synchronized AgentConfigure getInstance() {
        if (instance == null) {
            instance = new AgentConfigure();
            instance.setDaemon(true);
            instance.setName(ThreadUtil.getName(instance));
            instance.start();
        }
        return instance;
    }

    /**
     * sometimes call by sample application, at that time normally set some
     * properties directly
     */
    private AgentConfigure() {
        Properties p = new Properties();
        Map args = new HashMap();
        args.putAll(System.getenv());
        args.putAll(System.getProperties());
        p.putAll(args);
        this.property = p;
        reload(false);
    }

    private AgentConfigure(boolean b) {
    }

    public void run() {
        AgentLogger.println("Version " + Version.getAgentFullVersion());
        long dateUnit = DateUtil.getDateUnit();
        while (running) {
            reload(false);
            long nowUnit = DateUtil.getDateUnit();
            if (dateUnit != nowUnit) {
                dateUnit = nowUnit;
            }
            ThreadUtil.sleep(3000);
        }
    }

    public File getPropertyFile() {
        if (propertyFile != null) {
            return propertyFile;
        }
        String s = System.getProperty("scouter.config", agentDirPath + "/conf/scouter.conf");
        propertyFile = new File(s.trim());
        return propertyFile;
    }

    public synchronized boolean reload(boolean force) {
        long now = System.currentTimeMillis();
        if (force == false && now < lastCheck + 3000)
            return false;
        lastCheck = now;
        File file = getPropertyFile();
        if (file.lastModified() == lastLoadTime) {
            return false;
        }
        lastLoadTime = file.lastModified();
        Properties temp = new Properties();
        if (file.canRead()) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
                temp.load(in);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                FileUtil.close(in);
            }
        }
        property = ConfigValueUtil.replaceSysProp(temp);
        apply();
        ConfObserver.run();
        return true;
    }

    private void apply() {
        this._trace = getBoolean("_trace", false);
        this._trace_use_logger = getBoolean("_trace_use_logger", false);
        this.log_dir = getValue("log_dir", "");
        this.log_rotation_enabled = getBoolean("log_rotation_enabled", true);
        this.log_sysout = getBoolean("log_sysout", true);
        this.log_ignore_set = getStringSet("mgr_log_ignore_ids", ",");

        this.hook_enabled = getBoolean("hook_enabled", true);
        this.hook_injection_method_patterns = getValue("hook_injection_method_patterns");
        this.hook_injection_method_enabled = getBoolean("hook_injection_method_enabled", true);

//        this.hookSignature ^= this.hook_args_patterns.hashCode();
//        this.hookSignature ^= this.hook_return_patterns.hashCode();
//        this.hookSignature ^= this.hook_constructor_patterns.hashCode();
//        this.hookSignature ^= this.hook_connection_open_patterns.hashCode();
//        this.hookSignature ^= this.hook_method_patterns.hashCode();
//        this.hookSignature ^= this.hook_service_patterns.hashCode();
//        this.hookSignature ^= this.hook_apicall_patterns.hashCode();
//        this.hookSignature ^= this.hook_jsp_patterns.hashCode();
//        this.hookSignature ^= this.hook_jdbc_wrapping_driver_patterns.hashCode();
    }

    private StringSet getStringSet(String key, String deli) {
        StringSet set = new StringSet();
        String v = getValue(key);
        if (v != null) {
            String[] vv = StringUtil.split(v, deli);
            for (String x : vv) {
                x = StringUtil.trimToEmpty(x);
                if (x.length() > 0)
                    set.put(x);
            }
        }
        return set;
    }

    public String getValue(String key) {
        return StringUtil.trim(property.getProperty(key));
    }

    public String getValue(String key, String def) {
        return StringUtil.trim(property.getProperty(key, def));
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

    public String loadText() {
        File file = getPropertyFile();
        InputStream fin = null;
        try {
            fin = new FileInputStream(file);
            byte[] buff = FileUtil.readAll(fin);
            return new String(buff);
        } catch (Exception e) {
        } finally {
            FileUtil.close(fin);
        }
        return null;
    }

    public boolean saveText(String text) {
        File file = getPropertyFile();
        OutputStream out = null;
        try {
            if (file.getParentFile().exists() == false) {
                file.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file);
            out.write(text.getBytes());
            return true;
        } catch (Exception e) {
        } finally {
            FileUtil.close(out);
        }
        return false;
    }

    public void printConfig() {
        AgentLogger.info("Configure -Dscouter.bytescope.config=" + propertyFile);
    }

    private static HashSet<String> ignoreSet = new HashSet<String>();

    static {
        ignoreSet.add("property");
        ignoreSet.add("__experimental");
    }

    public MapValue getKeyValueInfo() {
        StringKeyLinkedMap<Object> defMap = ConfigValueUtil.getConfigDefault(new AgentConfigure(true));
        StringKeyLinkedMap<Object> curMap = ConfigValueUtil.getConfigDefault(this);
        MapValue m = new MapValue();
        ListValue nameList = m.newList("key");
        ListValue valueList = m.newList("value");
        ListValue defList = m.newList("default");
        StringEnumer enu = defMap.keys();
        while (enu.hasMoreElements()) {
            String key = enu.nextString();
            if (ignoreSet.contains(key))
                continue;
            nameList.add(key);
            valueList.add(ConfigValueUtil.toValue(curMap.get(key)));
            defList.add(ConfigValueUtil.toValue(defMap.get(key)));
        }
        return m;
    }

    public StringKeyLinkedMap<String> getConfigureDesc() {
        return ConfigValueUtil.getConfigDescMap(this);
    }

    public int getHookSignature() {
        return this.hookSignature;
    }

    public static void main(String[] args) {
        AgentConfigure o = new AgentConfigure(true);
        StringKeyLinkedMap<Object> defMap = ConfigValueUtil.getConfigDefault(o);
        StringKeyLinkedMap<String> descMap = ConfigValueUtil.getConfigDescMap(o);
        StringEnumer enu = defMap.keys();
        while (enu.hasMoreElements()) {
            String key = enu.nextString();
            if (ignoreSet.contains(key))
                continue;
            System.out.println(key + " : " + ConfigValueUtil.toValue(defMap.get(key)) + (descMap.containsKey(key) ? " (" + descMap.get(key) + ")" : ""));
        }
    }

    public boolean isIgnoreLog(String id) {
        return log_ignore_set.hasKey(id);
    }
}
