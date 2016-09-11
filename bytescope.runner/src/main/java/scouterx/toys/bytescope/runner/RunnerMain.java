package scouterx.toys.bytescope.runner;

import jline.console.ConsoleReader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import scouterx.org.pmw.tinylog.Logger;
import scouterx.toys.bytescope.command.support.AnsiPrint;
import scouterx.toys.bytescope.command.support.CommandChainExecutor;
import scouterx.toys.bytescope.command.support.CommandResult;
import scouterx.toys.bytescope.util.BytescopeContext;

import java.io.IOException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 4.
 */
public class RunnerMain {
    // $JAVA_HOME/bin/java -cp $JAVA_HOME/lib/tools.jar:./scouter.bytescope-runner-0.1-SNAPSHOT.jar:./lib/jline-2.12.jar:./lib/commons-lang3-3.4.jar scouterx.toys.bytescope.runner.RunnerMain
    // java -cp ./scouter.bytescope-runner-0.1-SNAPSHOT.jar:./lib/jline-2.12.jar:./lib/commons-lang3-3.4.jar scouterx.toys.bytescope.runner.RunnerMain

    public static void main(String[] args) throws IOException {
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
            if(StringUtils.isEmpty(line)) continue;
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
                    if(StringUtils.isNotBlank(result.getMessage())) {
                        BytescopeConsole.getWriter().write(result.getMessage() + "\n");
                    }
                }
            } catch(Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
