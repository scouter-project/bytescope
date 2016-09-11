package scouterx.toys.bytescope.command.support;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 10.
 */
public class CommandResult {
    public static byte SUCCESS = 1;
    public static byte FAIL = -1;
    public static byte NO_COMMAND = -2;

    private byte result;
    private String message;

    private CommandResult(){}

    public CommandResult(String message, byte result) {
        this.message = message;
        this.result = result;
    }

    public byte getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        private Builder() {}
        private byte result;
        private String message;

        public Builder setResult(byte result) {
            this.result = result;
            return this;
        }

        public Builder setResultSuccess() {
            this.result = SUCCESS;
            return this;
        }

        public Builder setResultFail() {
            this.result = FAIL;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public CommandResult build() {
            return new CommandResult(this.message, this.result);
        }
    }
}
