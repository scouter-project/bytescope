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

import scouter.toys.bytescope.AgentMain;
import scouter.toys.bytescope.util.BytesClassLoader;
import scouter.toys.bytescope.util.FileUtil;
import scouter.toys.bytescope.util.HashUtil;
import scouterx.org.pmw.tinylog.Logger;

import java.io.InputStream;
import java.util.LinkedHashMap;

/**
 * @author Paul S.J. Kim(sjkim@whatap.io)
 * @author Gun Lee(gunlee01@gmail.com)
 */

public class LoaderManager {
	private static ClassLoader toolsLoader;
	private static LinkedHashMap<Integer, ClassLoader> loaders = new LinkedHashMap<Integer, ClassLoader>();

	public static ClassLoader getHttpLoader(ClassLoader parent) {
		return createLoader(parent, "scouter.http");
	}

	private synchronized static ClassLoader createLoader(ClassLoader parent, String key) {

		int hashKey = (parent == null ? 0 : System.identityHashCode(parent));
		hashKey = hashKey ^ HashUtil.hash(key);
		ClassLoader loader = loaders.get(hashKey);
		if (loader == null) {
			try {
				byte[] bytes = deployJarBytes(key);
				if (bytes != null) {
					loader = new BytesClassLoader(bytes, parent);
					loaders.put(hashKey, loader);
				}
			} catch (Throwable e) {
				Logger.error(e, "SUBLOADER " + key + " " + e.getMessage());
			}
		}
		return loader;
	}

	private static byte[] deployJarBytes(String jarname) {
		try {
			InputStream is = AgentMain.class.getResourceAsStream("/" + jarname + ".jar");
			byte[] newBytes = FileUtil.readAll(is);
			is.close();
            Logger.info("LoadJarBytes " + jarname);
			return newBytes;
		} catch (Exception e) {
            Logger.error(e, "Error on deployJarBytes " + jarname);
			return null;
		}
	}

}
