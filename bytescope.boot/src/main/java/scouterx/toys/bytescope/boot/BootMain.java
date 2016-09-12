package scouterx.toys.bytescope.boot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 4.
 */
public class BootMain {

    public static void main(String[] args) throws Throwable {
        String lib = "./lib";
        if (args.length >= 1) {
            lib = args[0];
        }

        URL[] jarfiles = getURLs(lib);
        for(URL u : jarfiles) {
            System.out.println(u.toString());
        }
        URLClassLoader classloader = new URLClassLoader(jarfiles, BootMain.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(classloader);

        try {
            Class c = Class.forName("scouterx.toys.bytescope.runner.RunnerMain", true, classloader);
            Class[] argc = { String[].class };
            Object[] argo = { args };
            java.lang.reflect.Method method = c.getDeclaredMethod("main", argc);
            method.invoke(null, argo);

        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            usage();
        }
    }

    private static void usage() {
        System.out.println("java -cp ./boot.jar scouter.boot.Boot [./lib] ");
    }

    private static URL[] getURLs(String path) throws IOException {
        TreeMap<String, File> jars = new TreeMap<String, File>();
        File[] files = new File(path).listFiles();
        for (int i = 0; files != null && i < files.length; i++) {
            if (files[i].getName().startsWith("."))
                continue;
            jars.put(files[i].getName(), files[i]);
        }

        URL[] urls = new URL[jars.size()+1];
        ArrayList<File> v = new ArrayList<File>(jars.values());
        for (int i = 0; i < urls.length-1; i++) {
            urls[i] = v.get(i).toURI().toURL();
        }

        //get tools.jar
        String home = System.getProperty("java.home");
        String agent = home + File.separator + "lib" + File.separator + "tools.jar";
        File toolsJarFile = new File(agent);

        if (!toolsJarFile.exists()) {
            agent = home + File.separator + ".." + File.separator + "lib" + File.separator + "tools.jar";
            toolsJarFile = new File(agent);

            if (!toolsJarFile.exists()) {
                agent = home + File.separator + "jre" + File.separator + "lib" + File.separator + "tools.jar";
                toolsJarFile = new File(agent);

                if (!toolsJarFile.exists()) {
                    throw new RuntimeException("tools.jar missing ! use JDK instead of JRE or manually register tools.jar onto the classpath.");
                }
            }
        }

        urls[urls.length-1] = toolsJarFile.toURI().toURL();

        return urls;
    }
}
