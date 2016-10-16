package scouter.toys.bytescope.deco;

import scouter.toys.bytescope.deco.context.ServletTraceContext;
import scouter.toys.bytescope.deco.context.ServletTraceContextManager;
import scouter.toys.bytescope.deco.helper.HttpTraceFactory;
import scouter.toys.bytescope.deco.helper.IHttpTrace;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 4.
 */
public class CommonDeco {
    private static Object lock = new Object();
    private static IHttpTrace http = null;

    public static void startHttpService(Object req, Object res) {
        ServletTraceContext ctx = ServletTraceContextManager.getContext();
        if (ctx != null) {
            return;
        }
        ctx = ServletTraceContextManager.start();

        if (http == null) {
            initHttp(req);
        }
        http.start(ctx, req, res);

        String orgThreadName = Thread.currentThread().getName();
        ctx.setOrgThreadName(orgThreadName);

        Date startDate = new Date(ctx.getStartTime());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String startDateString = formatter.format(startDate);

        Thread.currentThread().setName(orgThreadName + " [bytescope][uri]" + ctx.getServiceName() + " [start at] " + startDateString + " [" + ctx.getStartTime() + "]");
    }

    public static void endHttpService(Throwable thr) {
        ServletTraceContext ctx = ServletTraceContextManager.end();
        if(ctx == null) {
            return;
        }

        Thread.currentThread().setName(ctx.getOrgThreadName());

        if(thr != null) {
        } else {
        }
    }

    private static void initHttp(Object req) {
        synchronized (lock) {
            if (http == null) {
                http = HttpTraceFactory.create(req.getClass().getClassLoader());
            }
        }
    }

}
