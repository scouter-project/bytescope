package scouter.toys.bytescope.mbean;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 11.
 */
public interface BytescopeContextMBean {
    String getAgentProcessName();

    boolean getLoaded();

    void setLogFilePath(String file);
    String getLogFilePath();

    void setLogLevel(String level);
    String getLogLevel();
}
