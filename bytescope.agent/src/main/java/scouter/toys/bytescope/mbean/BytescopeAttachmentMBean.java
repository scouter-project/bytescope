package scouter.toys.bytescope.mbean;

import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 11.
 */
public interface BytescopeAttachmentMBean {

    void attachEnhancedThreadNameForServlet();
    void attachEnhancedThreadNameForServletFilter();
    void redefineClasses(List<String> classNameList);
    void redefineImplementations(List<String> interfaceNameList);

}
