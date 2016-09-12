package scouterx.toys.bytescope.runner;

import jline.console.ConsoleReader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import scouterx.org.pmw.tinylog.Logger;
import scouterx.toys.bytescope.command.support.AnsiPrint;
import scouterx.toys.bytescope.command.support.CommandChainExecutor;
import scouterx.toys.bytescope.command.support.CommandResult;
import scouterx.toys.bytescope.util.BytescopeContext;
import scouterx.toys.util.$;

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
public class RunnerMain {
    // $JAVA_HOME/bin/java -cp $JAVA_HOME/lib/tools.jar:./scouter.bytescope-runner-0.1-SNAPSHOT.jar:./lib/jline-2.12.jar:./lib/commons-lang3-3.4.jar scouterx.toys.bytescope.runner.RunnerMain
    // java -cp ./scouter.bytescope-runner-0.1-SNAPSHOT.jar:./lib/jline-2.12.jar:./lib/commons-lang3-3.4.jar scouterx.toys.bytescope.runner.RunnerMain

    public static void main(String[] args) throws Throwable {
        String lib = "./lib";
        if (args.length >= 1) {
            lib = args[0];
        }

        URL[] jarfiles = getURLs(lib);
        URLClassLoader classloader = new URLClassLoader(jarfiles, RunnerMain.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(classloader);

        try {
            Class c = Class.forName("scouterx.toys.bytescope.runner.RunnerMain", true, classloader);
            Class[] argc = { String[].class };
            Object[] argo = { args };
            java.lang.reflect.Method method = c.getDeclaredMethod("realMain", argc);
            method.invoke(null, argo);

        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            usage();
        }
    }

    public static void realMain(String[] args) throws IOException {
        RunnerConfigure.getInstance();

        Logger.info("starting bootscope runner...");

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                System.out.println("Shutdown progressing...");
                BytescopeContext.close();
            }
        });

        AnsiPrint.enable = (!SystemUtils.IS_OS_WINDOWS);

        if (ArrayUtils.contains(args, "-ansi"))
            AnsiPrint.enable = true;
        else if (ArrayUtils.contains(args, "-noansi"))
            AnsiPrint.enable = false;

        ConsoleReader console = BytescopeConsole.getConsole();
        console.setPrompt(AnsiPrint.green("bytescope> "));

        //console.addCompleter(new AggregateCompleter(new ArgumentCompleter()));

        String line;

        Logger.info("bootscope runner started.");
        while (true) {
            line = console.readLine().trim();
            if($.isEmpty(line)) continue;
            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("bye")) {
                Logger.info("Shutdown progressing...");
                return;
            } else if (line.equalsIgnoreCase("cls")) {
                console.clearScreen();
                continue;
            }

            try {
                CommandResult result = CommandChainExecutor.getInstance().execute(line);
                if(result.getResult() < 0) {
                    BytescopeConsole.getWriter().write(AnsiPrint.red("[error] ") + result.getMessage() + "\n");
                } else {
                    if($.isNotBlank(result.getMessage())) {
                        BytescopeConsole.getWriter().write(result.getMessage() + "\n");
                    }
                }
            } catch(Throwable t) {
                t.printStackTrace();
            }
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

        URL[] urls = new URL[jars.size()];
        ArrayList<File> v = new ArrayList<File>(jars.values());
        for (int i = 0; i < urls.length; i++) {
            urls[i] = v.get(i).toURI().toURL();
        }
        return urls;
    }
}
