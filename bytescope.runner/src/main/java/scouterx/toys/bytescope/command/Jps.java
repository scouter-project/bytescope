package scouterx.toys.bytescope.command;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import scouterx.toys.bytescope.command.support.CommandResult;
import scouterx.toys.bytescope.util._;

import java.io.IOException;
import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 10.
 */
public class Jps {
    public CommandResult jps(String [] args) {
        List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
        StringBuilder builder = new StringBuilder();

        for(VirtualMachineDescriptor vmd : vmds) {
            try {
                VirtualMachine vm = VirtualMachine.attach(vmd);
            } catch (AttachNotSupportedException e) {
                e.printStackTrace();
                return CommandResult.getBuilder()
                        .setResultFail()
                        .setMessage("Attach not supprted!")
                        .build();
            } catch (IOException e) {
                return CommandResult.getBuilder()
                        .setResultFail()
                        .setMessage("[IO Exception]" + e.getMessage())
                        .build();
            }
            builder.append(vmd.id()).append(" ").append(vmd.displayName()).append(_.lineSeparator);
        }

        return CommandResult.getBuilder()
                .setResultSuccess()
                .setMessage(builder.toString())
                .build();
    }
}
