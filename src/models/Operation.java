package models;

import exceptions.OverflowException;

public class Operation {
    private int binaryCode;
    private int len;

    public Operation(String code, int len) {
        this.binaryCode = Integer.parseInt(code, 16);
        if (binaryCode < 0)
            throw new RuntimeException("Отрицательный код команды: " + code);
        this.len = len;
    }

    public int getBinaryCode() {
        return binaryCode;
    }

    public void setBinaryCode(int binaryCode) {
        this.binaryCode = binaryCode;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }
}
