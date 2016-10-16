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
package scouter.toys.bytescope.deco.helper;

import scouter.toys.bytescope.deco.context.ServletTraceContext;
import scouterx.org.pmw.tinylog.Logger;

public class HttpTraceFactory {
	private static final String HTTP_TRACE = "scouter.xtra.http.HttpTrace";

	public static final IHttpTrace dummy = new IHttpTrace() {
		public String getParameter(Object req, String key) {
			return null;
		}

		public String getHeader(Object req, String key) {
			return null;
		}

		public void start(ServletTraceContext ctx, Object req, Object res) {
		}

		public void end() {
		}
	};

	public static IHttpTrace create(ClassLoader parent) {
		try {
			ClassLoader loader = LoaderManager.getHttpLoader(parent);
			if (loader == null) {
				return dummy;
			}
			Class c = Class.forName(HTTP_TRACE, true, loader);
			return (IHttpTrace) c.newInstance();
		} catch (Throwable e) {
			Logger.error(e, "fail to create IHttpTrace");
			return dummy;
		}
	}

}
