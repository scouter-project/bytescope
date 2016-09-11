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
package scouter.toys.bytescope.util;

import scouter.toys.bytescope.AgentConfigure;
import scouter.util.*;

import java.io.*;

public class AgentLogger {

	private static StringLongLinkedMap lastLog = new StringLongLinkedMap().setMax(1000);
    static AgentConfigure conf = AgentConfigure.getInstance();

    public static void println(Object message) {
		println(build("SCOUTER", toString(message)), conf.log_sysout);
	}

	public static void println(String id, Object message) {
		if (checkOk(id, 0) == false) {
			return;
		}
		println(build(id, toString(message)), conf.log_sysout);
	}

    public static void println(String id, String message, Throwable t) {
        if (checkOk(id, 10) == false) {
            return;
        }
        println(build(id, message), conf.log_sysout);
        println(ThreadUtil.getStackTrace(t), conf.log_sysout);
    }

    public static void trace(Object message) {
        if(conf._trace) {
            if(conf._trace_use_logger) {
                println(build("SCOUTER-TRC", toString(message)), conf.log_sysout);
            } else {
                System.out.println(build("SCOUTER-TRC", toString(message)));
            }
        }
    }

	private static String toString(Object message) {
		return message == null ? "null" : message.toString();
	}

	private static String build(String id, String message) {
		return new StringBuffer(20 + id.length() + message.length())
				.append(DateUtil.datetime(System.currentTimeMillis())).append(" [").append(id).append("] ")
				.append(message).toString();
	}

	public static String getCallStack(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			t.printStackTrace(pw);
			return sw.toString();
		} finally {
			pw.close();
		}
	}

	private static boolean checkOk(String id, int sec) {
		if (AgentConfigure.getInstance().isIgnoreLog(id))
			return false;
		if (sec > 0) {
			long last = lastLog.get(id);
			long now = System.currentTimeMillis();
			if (now < last + sec * 1000)
				return false;
			lastLog.put(id, now);
		}
		return true;
	}

	static PrintWriter pw = null;
	static File logfile = null;

	private static void println(String msg, boolean sysout) {
		try {
			if (pw != null) {
				if (sysout) {
					System.out.println(msg);
				}
				pw.println(msg);
				pw.flush();
				return;
			} else {
				if (sysout) {
					System.out.println(msg);
				}
			}
		} catch (Throwable e) {
			pw = (PrintWriter) FileUtil.close(pw);
			if (sysout) {
				System.out.println(msg);
			}
		}
	}

	private static synchronized void openFile(String prefix) throws IOException {
		if (pw == null && StringUtil.isEmpty(conf.log_dir) == false) {
			File root = new File(conf.log_dir);
			if (root.canWrite() == false) {
				root.mkdirs();
			}
			if (root.canWrite() == false) {
				return;
			}

			if (conf.log_rotation_enabled) {
				File file = new File(conf.log_dir, "scouter-" + prefix + "-" + DateUtil.yyyymmdd() + ".log");
				FileWriter fw = new FileWriter(file, true);
				pw = new PrintWriter(fw);
				logfile = file;
			} else {
				File file = new File(conf.log_dir, "scouter-" + prefix + ".log");
				pw = new PrintWriter(new FileWriter(file, true));
				logfile = file;
			}

		}
	}

	static Runnable initializer = new Runnable() {
		long last = System.currentTimeMillis();
		long lastDataUnit = DateUtil.getDateUnit();
		String lastDir = conf.log_dir;
		boolean lastFileRotation = conf.log_rotation_enabled;
		String scouterName = "bytescope";

		public void run() {
			try {
				process();
			} catch (Throwable t) {
			}
		}

		private synchronized void process() {
			long now = System.currentTimeMillis();
			if (now > last + DateUtil.MILLIS_PER_HOUR) {
				last = now;
			}

			if (CompareUtil.equals(lastDir, conf.log_dir) == false //
					|| lastFileRotation != conf.log_rotation_enabled //
					|| lastDataUnit != DateUtil.getDateUnit() //
					|| (logfile != null && logfile.exists() == false)) {
				pw = (PrintWriter) FileUtil.close(pw);
				logfile = null;
				lastDir = conf.log_dir;
				lastFileRotation = conf.log_rotation_enabled;
				lastDataUnit = DateUtil.getDateUnit();
			}

			try {
				openFile(scouterName);
			} catch (Throwable t) {
				sysout(t.getMessage());
			}
		}
	};

	public static void main(String[] args) {
		String name = "scouter-19701123.log";
		int x = name.lastIndexOf('.');
		String date = name.substring("scouter-".length(), x);
		System.out.println(date);

	}

	public static void info(String message) {
		message = build("SCOUTER", message);
		sysout(message);
		println(message, false);
	}

	private static void sysout(String message) {
		System.out.println(message);
	}

	public static class FileLog implements IClose {
		private PrintWriter out;

		public FileLog(String filename) {
			try {
				this.out = new PrintWriter(new FileWriter(filename));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void println(String message) {
			if (this.out == null)
				return;
			this.out.println(DateUtil.datetime(System.currentTimeMillis()) + " " + message);
			this.out.flush();
		}

		public void close() {
			FileUtil.close(this.out);
		}
	}

}