package scouterx.toys.bytescope.runner;

import jline.console.ConsoleReader;
import scouterx.toys.bytescope.util.__;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 10.
 */
public class BytescopeConsole {
    public static BytescopeConsole instance = new BytescopeConsole();
    private ConsoleReader console;
    private PrintWriter writer;

    private BytescopeConsole() {
        try {
            console = new ConsoleReader();
            writer = new PrintWriter(console.getOutput());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static ConsoleReader getConsole() {
        return instance.console;
    }

    public static PrintWriter getWriter() {
        return instance.writer;
    }

    public static void println(String str) {
        instance.writer.write(str + __.lineSeparator);
    }
}
