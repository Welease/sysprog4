package models;

import exceptions.OverflowException;
import exceptions.UnknownCommandException;

public class PseudoCommand {
    private String name;
    private String value;
    private int len;
    private static String startAddress;
    private static String currentAddress;
    private boolean isRecord;

    public PseudoCommand(String name, String value) throws UnknownCommandException, OverflowException {
        this.name = name;
        this.value = value;
        int num = 0;
        switch (name.toLowerCase()) {
            case "word":
                isRecord = true;
                len = 3;
                try{
                    if (value.contains("h"))
                        num = Integer.parseInt(value.substring(0, value.length() - 1), 16);
                    else
                        num = Integer.parseInt(value);
                } catch (Exception e) {
                    throw new UnknownCommandException("Невозможно преобразовать значение в число.");
                }
                if (num > 16777215)
                    throw new OverflowException("Невозможно выделить память для значения " + value + ". Переполнение.");
                break;
            case "byte":
                isRecord = true;
                boolean checkValid = (value.charAt(1) != '\'') || (value.charAt(value.length() - 1) != '\'');
                if ('x' == value.charAt(0)) {
                    if (checkValid)
                        throw new UnknownCommandException("Ошибка при вводе значения: " + value);
                    String str = value.substring(2, value.length() - 1).toLowerCase();
                    for (int i = 0; i < str.length(); i++) {
                        char t = str.charAt(i);
                        if (((t < 'a') || (t > 'f')) && ((t < '0') || (t > '9')))
                            throw new UnknownCommandException("Некорректное значение операнда:" + value);
                    }
                    this.value = str;
                    len = (int) Math.ceil(str.length() / 2.0);
                }
                else if ('c' == value.charAt(0)) {
                    if (checkValid)
                        throw new UnknownCommandException("Ошибка при вводе значения: " + value);
                    this.value = value.substring(2, value.length() - 1).toLowerCase();
                    len = value.length() - 3;
                }
                else {
                    len = 1;
                    try{
                        if (value.contains("h"))
                            num = Integer.parseInt(value.substring(0, value.length() - 1), 16);
                        else
                            num = Integer.parseInt(value);
                    } catch (Exception e) {
                        throw new UnknownCommandException("Невозможно преобразовать значение в число: " + value);
                    }
                    if (num > 255)
                        throw new OverflowException("Невозможно выделить память для значения " + value + ". Переполнение.");
                }
                break;
            case "resb":
                isRecord = false;
                try {
                    if (value.charAt(value.length() - 1) == 'h')
                        len = Integer.parseInt(value, 16);
                    else
                        len = Integer.parseInt(value);
                } catch (Exception e) {
                    throw new UnknownCommandException("Невозможное значение для выделения памяти:" + value);
                }
                if (!checkValidMemory(len))
                    throw new UnknownCommandException("Невозможно выделить память.");
                break;
            case "resw":
                isRecord = false;
                try {
                    if (value.charAt(value.length() - 1) == 'h')
                        len = Integer.parseInt(value, 16) * 3;
                    else
                        len = Integer.parseInt(value) * 3;
                } catch (Exception e) {
                    throw new UnknownCommandException("Невозможное значение для выделения памяти:" + value);
                }
                if (!checkValidMemory(len / 3))
                    throw new UnknownCommandException("Невозможно выделить память.");
                break;
            case "start":
                isRecord = false;
                try {
                    Integer.parseInt(value, 16);
                } catch (Exception e) {
                    throw new UnknownCommandException("Невозможное значение для адреса: " + value);
                }
                len = 0;
                break;
            case "end":
                isRecord = false;
                len = 0;
                if ("".equals(value))
                    break;
                try {
                    Integer.parseInt(value, 16);
                } catch (Exception e) {
                    throw new UnknownCommandException("Невозможное значение для адреса: " + value);
                }

        }
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getLen() {
        return len;
    }

    public static void setStartAddress(String start) {
        startAddress = start;
    }
    public static void setCurrentAddress(String current) {currentAddress = current;}

    private boolean checkValidMemory(int value) {
        int address = Integer.parseInt(currentAddress, 16);
        String str = Integer.toHexString(address + value);
        if (Integer.parseInt("FFFFFF", 16) - Integer.parseInt(str, 16) < 0)
            return false;
        return true;
    }
}
