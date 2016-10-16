package scouter.toys.bytescope.deco.context;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 25.
 */
public class ServletTraceContextManager {
    private static ThreadLocal<ServletTraceContext> contextTL = new ThreadLocal<ServletTraceContext>();

    public static ServletTraceContext start() {
        if(contextTL.get() == null) {
            contextTL.set(new ServletTraceContext());
        }
        return contextTL.get();
    }

    public static ServletTraceContext getContext() {
        return contextTL.get();
    }

    public static ServletTraceContext end() {
        ServletTraceContext context = contextTL.get();
        contextTL.set(null);
        return context;
    }
}
