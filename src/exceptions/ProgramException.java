package exceptions;

public class ProgramException extends RuntimeException{
    public ProgramException(int index, String message) {
        super(index + ": " + message);
    }

    public ProgramException(String message) {
        super(message);
    }
}
