package models;

public class ConvertedCommand {
    private String command;
    private String value1;
    private String value2;

    public ConvertedCommand(String command, String value) {
        processCommand(command);
        this.value1 = value;
        this.value2 = "";
    }

    public ConvertedCommand(String command, String value1, String value2) {
        processCommand(command);
        this.value1 = value1;
        this.value2 = value2;
    }

    private void processCommand(String command) {
        try {
            int num = Integer.parseInt(command);
            String str = Integer.toHexString(num).toUpperCase();
            if (str.length() % 2 != 0)
                str = "0" + str;
            this.command = str;
        }catch (Exception e){
            this.command = command.toUpperCase();
        }
    }

    public String getCommand() {
        return command;
    }

    public String getValue1() {
        return value1;
    }

    public String getValue2() {
        return value2;
    }
}
