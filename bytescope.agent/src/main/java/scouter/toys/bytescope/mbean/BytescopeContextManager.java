package scouter.toys.bytescope.mbean;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 11.
 */
public class BytescopeContextManager {
    private static BytescopeContext context = null;

    private BytescopeContextManager() {}

    public synchronized static BytescopeContext getBytescopeContext() {
        if(context == null) {
            context = new BytescopeContext();
        }
        return context;
    }
}
