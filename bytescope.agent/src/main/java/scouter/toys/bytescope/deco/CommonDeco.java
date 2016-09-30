package scouter.toys.bytescope.deco;

import scouter.toys.bytescope.deco.context.ServletTraceContext;
import scouter.toys.bytescope.deco.context.ServletTraceContextManager;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 4.
 */
public class CommonDeco {
    public static void startHttpService() {
        ServletTraceContext contxt = ServletTraceContextManager.start();
        System.out.println("[method-start]CommonDeco.startHttpService()");
    }

    public static void endHttpService() {
        System.out.println("[method-start]CommonDeco.endHttpService()-Normal case");
    }
}
