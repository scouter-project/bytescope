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

package scouter.xtra.http;

import scouter.toys.bytescope.deco.context.ServletTraceContext;
import scouter.toys.bytescope.deco.helper.IHttpTrace;
import scouter.toys.bytescope.util.HashUtil;
import scouterx.org.pmw.tinylog.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpTrace implements IHttpTrace {

    public HttpTrace() {
    }

    public String getParameter(Object req, String key) {
        HttpServletRequest request = (HttpServletRequest) req;

        String ctype = request.getContentType();
        if (ctype != null && ctype.startsWith("application/x-www-form-urlencoded"))
            return null;

        return request.getParameter(key);
    }

    public String getHeader(Object req, String key) {
        HttpServletRequest request = (HttpServletRequest) req;
        return request.getHeader(key);
    }

    public void start(ServletTraceContext ctx, Object reqObj, Object resObj) {
        HttpServletRequest request = (HttpServletRequest) reqObj;
        HttpServletResponse response = (HttpServletResponse) resObj;

        if(ctx == null) {
            Logger.info("[HttpTrace.start]ctx is null - " + getRequestURI(request));
            return;
        }

        long startTimestamp = System.currentTimeMillis();
        ctx.setStartTime(startTimestamp);
        ctx.setServiceName(getRequestURI(request));
        ctx.setServicHash(HashUtil.hash(ctx.getServiceName()));
    }

    private String getRequestURI(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null)
            return "no-url";
        int x = uri.indexOf(';');
        if (x > 0)
            return uri.substring(0, x);
        else
            return uri;
    }


    public void end() {
        // HttpServletRequest request = (HttpServletRequest)req;
        // HttpServletResponse response = (HttpServletResponse)res;
    }

}