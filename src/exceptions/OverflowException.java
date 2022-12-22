package exceptions;

import java.io.IOException;

public class OverflowException extends IOException {
    public OverflowException(String message) {
        super(message);
    }
}
