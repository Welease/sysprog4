package exceptions;

import java.io.IOException;

public class UnknownCommandException extends IOException {
    public UnknownCommandException(String message) {
        super(message);
    }
}
