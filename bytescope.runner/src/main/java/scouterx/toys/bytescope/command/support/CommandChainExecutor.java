package scouterx.toys.bytescope.command.support;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 10.
 */
public class CommandChainExecutor {
    private static CommandChainExecutor instance = new CommandChainExecutor();

    private CommandChainExecutor() {
    }

    public static CommandChainExecutor getInstance() {
        return instance;
    }

    public CommandResult execute(String line) {
        return execute(StringUtils.split(line));
    }

    public CommandResult execute(String[] args) {
        try {
            String className = this.getClass().getName();
            String commandClassName = StringUtils.substring(className, 0, StringUtils.lastOrdinalIndexOf(className, ".", 2) + 1)
                    + StringUtils.capitalize(args[0]);
            Class clazz = Class.forName(commandClassName);
            Object o = clazz.newInstance();

            String[] commands = new String[args.length];
            for (int i = 0; i < commands.length; i++) {
                if (i == 0) {
                    commands[i] = args[i];
                } else {
                    commands[i] = commands[i-1] + StringUtils.capitalize(args[i]);
                }
            }

            Method m = null;
            String[] passingArgs = null;
            for (int i = commands.length - 1; i >= 0; i--) {
                try {
                    m = clazz.getMethod(commands[i], String[].class);
                    passingArgs = new String[commands.length - i - 1];
                    for(int j=0; j<passingArgs.length; j++) {
                       passingArgs[j] = args[args.length - passingArgs.length + j];
                    }
                    break;
                } catch (NoSuchMethodException e) {
                    //continue
                } catch (SecurityException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            if(m != null) {
                return (CommandResult) m.invoke(o, new Object[] {passingArgs});
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            return CommandResult.getBuilder().setResult(CommandResult.NO_COMMAND).setMessage(args[0] + " : not exist command!").build();
        }
        return null;
    }

    public static void main(String[] args) {
        CommandChainExecutor.getInstance().execute(new String[]{"config", "set", "value", "once", "2500"});
    }
}
