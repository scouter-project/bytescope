package scouter.toys.bytescope.deco.context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 25.
 */
public class ServletTraceContext {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String serviceName;
    private int servicHash;
    private long startTime;
    private String orgThreadName;

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getServicHash() {
        return servicHash;
    }

    public void setServicHash(int servicHash) {
        this.servicHash = servicHash;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getOrgThreadName() {
        return orgThreadName;
    }

    public void setOrgThreadName(String orgThreadName) {
        this.orgThreadName = orgThreadName;
    }
}
