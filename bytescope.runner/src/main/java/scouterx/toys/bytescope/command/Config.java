package scouterx.toys.bytescope.command;

import org.apache.commons.lang3.Validate;
import scouterx.toys.bytescope.command.support.CommandResult;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 10.
 */
public class Config {

    public CommandResult config(String[] args) {
        Validate.validIndex(args, 1);
        Validate.isTrue(args.length == 1, "argument count unmatched");

        System.out.println("[invoked][set]");
        return null;
    }

    public CommandResult configSet(String[] args) {
        System.out.println("[invoked][configSet]");
        return null;
    }

    public CommandResult configSetValue(String[] args) {
        System.out.println("[invoked][configSet]");
        return null;
    }

    public CommandResult configSetValueOnce(String[] args) {
        System.out.println("[invoked][configSet]");
        return null;
    }
}
