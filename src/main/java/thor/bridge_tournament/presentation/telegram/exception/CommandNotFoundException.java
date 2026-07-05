package thor.bridge_tournament.presentation.telegram.exception;

import lombok.Getter;

@Getter
public class CommandNotFoundException extends RuntimeException {
    private final String command;

    public CommandNotFoundException(String command) {
        super("Command " + command + " not exists");
        this.command = command;
    }
}
